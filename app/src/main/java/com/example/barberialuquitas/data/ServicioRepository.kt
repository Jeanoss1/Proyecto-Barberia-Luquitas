package com.example.barberialuquitas.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val COLECCION_SERVICIOS = "servicios"

class ServicioRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observarServicios(): Flow<List<Servicio>> = callbackFlow {
        val registro = firestore.collection(COLECCION_SERVICIOS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val servicios = snapshot?.documents.orEmpty().map { documento ->
                    Servicio(
                        id = documento.id,
                        nombre = documento.getString("nombre") ?: "",
                        descripcion = documento.getString("descripcion") ?: "",
                        precio = documento.getLong("precio") ?: 0
                    )
                }
                trySend(servicios)
            }
        awaitClose { registro.remove() }
    }
}
