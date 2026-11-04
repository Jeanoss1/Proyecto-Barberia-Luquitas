package com.example.barberialuquitas.data.repositorio

import com.example.barberialuquitas.data.modelo.Barbero
import com.example.barberialuquitas.data.modelo.Colecciones
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "BarberoRepository"

class BarberoRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val barberos get() = firestore.collection(Colecciones.BARBEROS)

    // Para reservar (RF05): solo los barberos activos de la barbería.
    fun observarBarberosActivos(barberiaId: String): Flow<Result<List<Barbero>>> =
        barberos.whereEqualTo("barberiaId", barberiaId)
            .whereEqualTo("activo", true)
            .observar(TAG, "No se pudieron cargar los barberos.", ::aBarbero)
            .map { resultado -> resultado.map { lista -> lista.sortedBy { it.nombre } } }

    // Gestión del administrador (RF14): activos e inactivos.
    fun observarBarberosDeBarberia(barberiaId: String): Flow<Result<List<Barbero>>> =
        barberos.whereEqualTo("barberiaId", barberiaId)
            .observar(TAG, "No se pudieron cargar los barberos.", ::aBarbero)
            .map { resultado -> resultado.map { lista -> lista.sortedWith(compareBy({ !it.activo }, { it.nombre })) } }

    suspend fun obtenerBarbero(barberoId: String): Result<Barbero> =
        intentar(TAG, "No se pudo cargar la información del barbero.") {
            val documento = barberos.document(barberoId).get().await()
            if (!documento.exists()) error("El barbero $barberoId no existe")
            aBarbero(documento)
        }

    // Crea el barbero si no tiene id; si lo tiene, lo reemplaza con los nuevos datos.
    suspend fun guardarBarbero(barbero: Barbero): Result<Unit> =
        intentar(TAG, "No se pudo guardar el barbero. Revisa los datos e inténtalo de nuevo.") {
            val referencia = if (barbero.id.isBlank()) barberos.document() else barberos.document(barbero.id)
            referencia.set(barbero.aMapa()).await()
        }

    // Dar de alta (true) o de baja (false). Las citas ya reservadas se mantienen.
    suspend fun cambiarActivo(barberoId: String, activo: Boolean): Result<Unit> =
        intentar(TAG, "No se pudo actualizar el barbero.") {
            barberos.document(barberoId).update("activo", activo).await()
        }
}
