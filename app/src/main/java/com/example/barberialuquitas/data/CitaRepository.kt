package com.example.barberialuquitas.data

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "CitaRepository"

class HorarioOcupadoException : Exception("Ese horario acaba de ser reservado por otro cliente. Elige otro.")

class CitaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun reservarCita(cita: Cita): Result<String> = try {
        val refCita = firestore.collection(Colecciones.CITAS).document()
        val refCandado = firestore.collection(Colecciones.HORARIOS_OCUPADOS)
            .document(idHorarioOcupado(cita.barberoId, cita.fecha, cita.hora))

        firestore.runTransaction { transaccion ->
            if (transaccion.get(refCandado).exists()) throw HorarioOcupadoException()
            transaccion.set(
                refCandado,
                mapOf(
                    "citaId" to refCita.id,
                    "barberoId" to cita.barberoId,
                    "fecha" to cita.fecha,
                    "hora" to cita.hora
                )
            )
            transaccion.set(refCita, aMapaNuevaCita(cita))
        }.await()
        Result.success(refCita.id)
    } catch (e: Exception) {
        if (e is HorarioOcupadoException || e.cause is HorarioOcupadoException) {
            Log.e(TAG, "reservarCita: horario ocupado", e)
            Result.failure(HorarioOcupadoException())
        } else {
            Log.e(TAG, "reservarCita: fallo inesperado", e)
            Result.failure(Exception("No se pudo registrar la reserva. Inténtalo de nuevo."))
        }
    }

    suspend fun cancelarCita(cita: Cita): Result<Unit> = try {
        val refCita = firestore.collection(Colecciones.CITAS).document(cita.id)
        val refCandado = firestore.collection(Colecciones.HORARIOS_OCUPADOS)
            .document(idHorarioOcupado(cita.barberoId, cita.fecha, cita.hora))

        firestore.batch()
            .update(refCita, "estado", EstadosCita.CANCELADA)
            .delete(refCandado)
            .commit()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "cancelarCita: fallo inesperado", e)
        Result.failure(Exception("No se pudo cancelar la cita. Inténtalo de nuevo."))
    }

    fun observarCitasDelCliente(clienteId: String): Flow<Result<List<Cita>>> = callbackFlow {
        val registro = firestore.collection(Colecciones.CITAS)
            .whereEqualTo("clienteId", clienteId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observarCitasDelCliente: fallo al escuchar", error)
                    trySend(Result.failure(Exception("No se pudieron cargar tus citas.")))
                    close()
                    return@addSnapshotListener
                }
                trySend(Result.success(snapshot?.documents.orEmpty().map(::aCita)))
            }
        awaitClose { registro.remove() }
    }

    fun observarAgendaDelDia(fecha: String): Flow<Result<List<Cita>>> = callbackFlow {
        val registro = firestore.collection(Colecciones.CITAS)
            .whereEqualTo("fecha", fecha)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observarAgendaDelDia: fallo al escuchar", error)
                    trySend(Result.failure(Exception("No se pudo cargar la agenda.")))
                    close()
                    return@addSnapshotListener
                }
                val turnos = snapshot?.documents.orEmpty()
                    .map(::aCita)
                    .filter { it.estado != EstadosCita.CANCELADA }
                    .sortedBy { it.hora }
                trySend(Result.success(turnos))
            }
        awaitClose { registro.remove() }
    }

    fun observarHorasOcupadas(barberoId: String, fecha: String): Flow<Result<Set<String>>> = callbackFlow {
        val registro = firestore.collection(Colecciones.HORARIOS_OCUPADOS)
            .whereEqualTo("fecha", fecha)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observarHorasOcupadas: fallo al escuchar", error)
                    trySend(Result.failure(Exception("No se pudieron cargar los horarios.")))
                    close()
                    return@addSnapshotListener
                }
                val horas = snapshot?.documents.orEmpty()
                    .filter { it.getString("barberoId") == barberoId }
                    .mapNotNull { it.getString("hora") }
                    .toSet()
                trySend(Result.success(horas))
            }
        awaitClose { registro.remove() }
    }
}

private fun aCita(documento: DocumentSnapshot) = Cita(
    id = documento.id,
    clienteId = documento.getString("clienteId") ?: "",
    clienteNombre = documento.getString("clienteNombre") ?: "",
    servicioId = documento.getString("servicioId") ?: "",
    servicioNombre = documento.getString("servicioNombre") ?: "",
    precio = documento.getLong("precio") ?: 0,
    barberoId = documento.getString("barberoId") ?: "",
    barberoNombre = documento.getString("barberoNombre") ?: "",
    fecha = documento.getString("fecha") ?: "",
    hora = documento.getString("hora") ?: "",
    estado = documento.getString("estado") ?: EstadosCita.RESERVADA
)

private fun aMapaNuevaCita(cita: Cita) = mapOf(
    "clienteId" to cita.clienteId,
    "clienteNombre" to cita.clienteNombre,
    "servicioId" to cita.servicioId,
    "servicioNombre" to cita.servicioNombre,
    "precio" to cita.precio,
    "barberoId" to cita.barberoId,
    "barberoNombre" to cita.barberoNombre,
    "fecha" to cita.fecha,
    "hora" to cita.hora,
    "estado" to EstadosCita.RESERVADA,
    "creadaEn" to FieldValue.serverTimestamp()
)

fun idHorarioOcupado(barberoId: String, fecha: String, hora: String) = "${barberoId}_${fecha}_${hora}"
