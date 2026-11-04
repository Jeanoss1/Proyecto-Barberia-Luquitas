package com.example.barberialuquitas.data.repositorio

import android.util.Log
import com.example.barberialuquitas.data.modelo.Barberia
import com.example.barberialuquitas.data.modelo.Colecciones
import com.example.barberialuquitas.data.modelo.ID_BARBERIA_ORIGINAL
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "BarberiaRepository"

class BarberiaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val barberias get() = firestore.collection(Colecciones.BARBERIAS)

    // Lista que ve el cliente para elegir dónde atenderse.
    fun observarBarberiasActivas(): Flow<Result<List<Barberia>>> =
        barberias.whereEqualTo("activa", true)
            .observar(TAG, "No se pudieron cargar las barberías.", ::aBarberia)
            .map { resultado -> resultado.map { lista -> lista.sortedBy { it.nombre } } }

    fun observarBarberia(barberiaId: String): Flow<Result<Barberia?>> =
        barberias.document(barberiaId).observar(TAG, "No se pudo cargar la barbería.", ::aBarberia)

    suspend fun obtenerBarberia(barberiaId: String): Result<Barberia> =
        intentar(TAG, "No se pudo cargar la barbería.") {
            val documento = barberias.document(barberiaId).get().await()
            if (!documento.exists()) error("La barbería $barberiaId no existe")
            aBarberia(documento)
        }

    // RF17 y datos generales: el administrador actualiza su barbería.
    suspend fun guardarBarberia(barberia: Barberia): Result<Unit> =
        intentar(TAG, "No se pudo guardar la configuración. Revisa los datos e inténtalo de nuevo.") {
            barberias.document(barberia.id).update(barberia.aMapa()).await()
        }

    // Un administrador sin barbería registra la suya y queda asignado a ella (en un solo batch, ver firestore.rules).
    // Si todavía hay datos de antes de soportar varias barberías, la primera barbería registrada es la original
    // y luego adopta esos datos con adoptarDatosAntiguos().
    suspend fun registrarBarberia(barberia: Barberia, adminUid: String): Result<String> =
        intentar(TAG, "No se pudo registrar la barbería. Inténtalo de nuevo.") {
            val esOriginal = !barberias.document(ID_BARBERIA_ORIGINAL).get().await().exists() && hayDatosAntiguos()
            val referencia = if (esOriginal) barberias.document(ID_BARBERIA_ORIGINAL) else barberias.document()
            firestore.batch()
                .set(referencia, barberia.aMapa() + mapOf("creadaPor" to adminUid, "migrada" to !esOriginal))
                .update(firestore.collection(Colecciones.USUARIOS).document(adminUid), "barberiaId", referencia.id)
                .commit()
                .await()
            referencia.id
        }

    // Migración única: servicios, barberos y citas sin "barberiaId" pasan a la barbería original.
    // Cada documento se actualiza por separado para que uno con datos incompletos no detenga a los demás.
    suspend fun adoptarDatosAntiguos(barberia: Barberia): Result<Int> =
        intentar(TAG, "No se pudieron actualizar los datos anteriores de la barbería.") {
            if (barberia.id != ID_BARBERIA_ORIGINAL || barberia.migrada) return@intentar 0
            var fallidos = 0
            var adoptados = 0

            suspend fun adoptar(coleccion: String, camposExtra: (Map<String, Any?>) -> Map<String, Any>) {
                val documentos = firestore.collection(coleccion).get().await().documents
                    .filter { it.barberiaIdONull() == null }
                documentos.forEach { documento ->
                    val cambios = mapOf<String, Any>("barberiaId" to ID_BARBERIA_ORIGINAL) +
                        camposExtra(documento.data.orEmpty())
                    runCatching { documento.reference.update(cambios).await() }
                        .onSuccess { adoptados++ }
                        .onFailure { fallidos++; Log.e(TAG, "adoptar $coleccion/${documento.id}", it) }
                }
            }

            adoptar(Colecciones.SERVICIOS) { datos -> if ("activo" in datos) emptyMap() else mapOf("activo" to true) }
            adoptar(Colecciones.BARBEROS) { datos -> if ("activo" in datos) emptyMap() else mapOf("activo" to false) }
            adoptar(Colecciones.CITAS) { mapOf("barberiaNombre" to barberia.nombre) }

            if (fallidos > 0) error("$fallidos documento(s) no se pudieron adoptar")
            barberias.document(barberia.id).update("migrada", true).await()
            adoptados
        }

    private suspend fun hayDatosAntiguos(): Boolean =
        listOf(Colecciones.SERVICIOS, Colecciones.BARBEROS).any { coleccion ->
            firestore.collection(coleccion).get().await().documents.any { it.barberiaIdONull() == null }
        }
}
