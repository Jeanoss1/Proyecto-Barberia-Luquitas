package com.example.barberialuquitas.data.repositorio

import com.example.barberialuquitas.data.modelo.Colecciones
import com.example.barberialuquitas.data.modelo.Servicio
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "ServicioRepository"

class ServicioRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val servicios get() = firestore.collection(Colecciones.SERVICIOS)

    // Catálogo del cliente (RF04): solo los servicios activos de la barbería elegida.
    fun observarServiciosActivos(barberiaId: String): Flow<Result<List<Servicio>>> =
        servicios.whereEqualTo("barberiaId", barberiaId)
            .whereEqualTo("activo", true)
            .observar(TAG, "No se pudieron cargar los servicios.", ::aServicio)
            .map { resultado -> resultado.map { lista -> lista.sortedBy { it.nombre } } }

    // Gestión del administrador (RF13): activos e inactivos.
    fun observarServiciosDeBarberia(barberiaId: String): Flow<Result<List<Servicio>>> =
        servicios.whereEqualTo("barberiaId", barberiaId)
            .observar(TAG, "No se pudieron cargar los servicios.", ::aServicio)
            .map { resultado -> resultado.map { lista -> lista.sortedWith(compareBy({ !it.activo }, { it.nombre })) } }

    suspend fun obtenerServicio(servicioId: String): Result<Servicio> =
        intentar(TAG, "No se pudo cargar el servicio.") {
            val documento = servicios.document(servicioId).get().await()
            if (!documento.exists()) error("El servicio $servicioId no existe")
            aServicio(documento)
        }

    // Crea el servicio si no tiene id; si lo tiene, lo reemplaza con los nuevos datos.
    suspend fun guardarServicio(servicio: Servicio): Result<Unit> =
        intentar(TAG, "No se pudo guardar el servicio. Revisa los datos e inténtalo de nuevo.") {
            val referencia = if (servicio.id.isBlank()) servicios.document() else servicios.document(servicio.id)
            referencia.set(servicio.aMapa()).await()
        }

    suspend fun cambiarActivo(servicioId: String, activo: Boolean): Result<Unit> =
        intentar(TAG, "No se pudo actualizar el servicio.") {
            servicios.document(servicioId).update("activo", activo).await()
        }
}
