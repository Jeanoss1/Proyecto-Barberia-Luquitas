package com.example.barberialuquitas.data

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "BarberoRepository"

class BarberoRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observarBarberos(): Flow<Result<List<Barbero>>> = callbackFlow {
        val registro = firestore.collection(Colecciones.BARBEROS)
            .whereEqualTo("activo", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observarBarberos: fallo al escuchar", error)
                    trySend(Result.failure(Exception("No se pudieron cargar los barberos.")))
                    close()
                    return@addSnapshotListener
                }
                val barberos = snapshot?.documents.orEmpty().map(::aBarbero).sortedBy { it.nombre }
                trySend(Result.success(barberos))
            }
        awaitClose { registro.remove() }
    }

    suspend fun obtenerBarbero(barberoId: String): Result<Barbero> = try {
        val documento = firestore.collection(Colecciones.BARBEROS).document(barberoId).get().await()
        if (!documento.exists()) error("El barbero $barberoId no existe")
        Result.success(aBarbero(documento))
    } catch (e: Exception) {
        Log.e(TAG, "obtenerBarbero: fallo inesperado", e)
        Result.failure(Exception("No se pudo cargar la información del barbero."))
    }

    private fun aBarbero(documento: DocumentSnapshot) = Barbero(
        id = documento.id,
        nombre = documento.getString("nombre") ?: "",
        especialidad = documento.getString("especialidad") ?: "",
        fotoUrl = documento.getString("fotoUrl")?.takeIf { it.isNotBlank() },
        activo = documento.getBoolean("activo") ?: false,
        horaInicio = documento.getString("horaInicio") ?: "09:00",
        horaFin = documento.getString("horaFin") ?: "20:00",
        diasLaborables = (documento.get("diasLaborables") as? List<*>).orEmpty()
            .mapNotNull { (it as? Number)?.toInt() }
    )
}
