package com.example.barberialuquitas.data

import android.util.Log
import com.example.barberialuquitas.util.DURACION_BLOQUE_MINUTOS
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "ServicioRepository"

class ServicioRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observarServicios(): Flow<List<Servicio>> = callbackFlow {
        val registro = firestore.collection(Colecciones.SERVICIOS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents.orEmpty().map(::aServicio))
            }
        awaitClose { registro.remove() }
    }

    suspend fun obtenerServicio(servicioId: String): Result<Servicio> = try {
        val documento = firestore.collection(Colecciones.SERVICIOS).document(servicioId).get().await()
        if (!documento.exists()) error("El servicio $servicioId no existe")
        Result.success(aServicio(documento))
    } catch (e: Exception) {
        Log.e(TAG, "obtenerServicio: fallo inesperado", e)
        Result.failure(Exception("No se pudo cargar el servicio."))
    }

    private fun aServicio(documento: DocumentSnapshot) = Servicio(
        id = documento.id,
        nombre = documento.getString("nombre") ?: "",
        descripcion = documento.getString("descripcion") ?: "",
        precio = documento.getLong("precio") ?: 0,
        duracionMinutos = documento.getLong("duracionMinutos")?.toInt() ?: DURACION_BLOQUE_MINUTOS
    )
}
