package com.example.barberialuquitas.data.repositorio

import android.util.Log
import com.example.barberialuquitas.data.modelo.Cita
import com.example.barberialuquitas.data.modelo.Colecciones
import com.example.barberialuquitas.data.modelo.EstadosCita
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "CitaRepository"

class HorarioOcupadoException : Exception("Ese horario acaba de ser reservado por otro cliente. Elige otro.")

class CitaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val citas get() = firestore.collection(Colecciones.CITAS)

    // ───────────── Cliente ─────────────

    // RF06/RF07: crea la cita y un "candado" por cada bloque de 30 minutos que ocupa.
    // Si cualquiera de esos bloques ya existe, la transacción falla y nadie reserva dos veces el mismo horario.
    suspend fun reservarCita(cita: Cita): Result<String> = try {
        val refCita = citas.document()
        val candados = cita.bloques.associateWith { hora -> refCandado(cita.barberoId, cita.fecha, hora) }

        firestore.runTransaction { transaccion ->
            if (candados.values.any { transaccion.get(it).exists() }) throw HorarioOcupadoException()
            candados.forEach { (hora, ref) ->
                transaccion.set(
                    ref,
                    mapOf(
                        "citaId" to refCita.id,
                        "barberoId" to cita.barberoId,
                        "fecha" to cita.fecha,
                        "hora" to hora
                    )
                )
            }
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

    // RF09: el cliente cancela su cita y libera sus bloques en el mismo batch.
    suspend fun cancelarCita(cita: Cita): Result<Unit> =
        intentar(TAG, "No se pudo cancelar la cita. Inténtalo de nuevo.") {
            val lote = firestore.batch().update(citas.document(cita.id), "estado", EstadosCita.CANCELADA)
            cita.bloques.forEach { hora -> lote.delete(refCandado(cita.barberoId, cita.fecha, hora)) }
            lote.commit().await()
        }

    // RF08
    fun observarCitasDelCliente(clienteId: String): Flow<Result<List<Cita>>> =
        citas.whereEqualTo("clienteId", clienteId)
            .observar(TAG, "No se pudieron cargar tus citas.", ::aCita)

    // RF06: bloques ya reservados de un barbero en una fecha.
    fun observarHorasOcupadas(barberoId: String, fecha: String): Flow<Result<Set<String>>> =
        firestore.collection(Colecciones.HORARIOS_OCUPADOS)
            .whereEqualTo("fecha", fecha)
            .observar(TAG, "No se pudieron cargar los horarios.") { it }
            .map { resultado ->
                resultado.map { candados ->
                    candados.filter { it.getString("barberoId") == barberoId }.mapNotNull { it.getString("hora") }.toSet()
                }
            }

    // ───────────── Administrador ─────────────

    // RF15 / RF12: todas las citas de la barbería en una fecha, ordenadas por hora.
    fun observarAgendaDelDia(barberiaId: String, fecha: String): Flow<Result<List<Cita>>> =
        citas.whereEqualTo("barberiaId", barberiaId)
            .whereEqualTo("fecha", fecha)
            .observar(TAG, "No se pudo cargar la agenda.", ::aCita)
            .map { resultado -> resultado.map { lista -> lista.sortedWith(compareBy({ it.hora }, { it.barberoNombre })) } }

    // RF19: citas entre dos fechas (inclusive). Requiere el índice compuesto de firestore.indexes.json.
    fun observarCitasEntre(barberiaId: String, desde: String, hasta: String): Flow<Result<List<Cita>>> =
        citas.whereEqualTo("barberiaId", barberiaId)
            .whereGreaterThanOrEqualTo("fecha", desde)
            .whereLessThanOrEqualTo("fecha", hasta)
            .observar(TAG, "No se pudo cargar el reporte.", ::aCita)

    // RF16: marcar como atendida o no asistió.
    suspend fun cambiarEstado(cita: Cita, nuevoEstado: String): Result<Unit> =
        intentar(TAG, "No se pudo actualizar el estado de la cita.") {
            require(nuevoEstado == EstadosCita.ATENDIDA || nuevoEstado == EstadosCita.NO_ASISTIO)
            citas.document(cita.id)
                .update(mapOf("estado" to nuevoEstado, "actualizadaEn" to FieldValue.serverTimestamp()))
                .await()
        }

    // RF16: el administrador cancela una cita reservada y libera sus bloques para otros clientes.
    suspend fun cancelarCitaComoAdministrador(cita: Cita): Result<Unit> =
        intentar(TAG, "No se pudo cancelar la cita. Inténtalo de nuevo.") {
            val lote = firestore.batch().update(
                citas.document(cita.id),
                mapOf("estado" to EstadosCita.CANCELADA, "actualizadaEn" to FieldValue.serverTimestamp())
            )
            cita.bloques.forEach { hora -> lote.delete(refCandado(cita.barberoId, cita.fecha, hora)) }
            lote.commit().await()
        }

    private fun refCandado(barberoId: String, fecha: String, hora: String) =
        firestore.collection(Colecciones.HORARIOS_OCUPADOS).document(idHorarioOcupado(barberoId, fecha, hora))
}

private fun aMapaNuevaCita(cita: Cita) = mapOf(
    "barberiaId" to cita.barberiaId,
    "barberiaNombre" to cita.barberiaNombre,
    "clienteId" to cita.clienteId,
    "clienteNombre" to cita.clienteNombre,
    "servicioId" to cita.servicioId,
    "servicioNombre" to cita.servicioNombre,
    "precio" to cita.precio,
    "barberoId" to cita.barberoId,
    "barberoNombre" to cita.barberoNombre,
    "fecha" to cita.fecha,
    "hora" to cita.hora,
    "duracionMinutos" to cita.duracionMinutos,
    "bloques" to cita.bloques,
    "estado" to EstadosCita.RESERVADA,
    "creadaEn" to FieldValue.serverTimestamp()
)

fun idHorarioOcupado(barberoId: String, fecha: String, hora: String) = "${barberoId}_${fecha}_${hora}"
