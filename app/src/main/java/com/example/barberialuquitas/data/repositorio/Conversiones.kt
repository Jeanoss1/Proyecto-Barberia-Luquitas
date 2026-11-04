package com.example.barberialuquitas.data.repositorio

import com.example.barberialuquitas.data.modelo.Barberia
import com.example.barberialuquitas.data.modelo.Barbero
import com.example.barberialuquitas.data.modelo.Cita
import com.example.barberialuquitas.data.modelo.EstadosCita
import com.example.barberialuquitas.data.modelo.ID_BARBERIA_ORIGINAL
import com.example.barberialuquitas.data.modelo.ORDEN_POR_DEFECTO
import com.example.barberialuquitas.data.modelo.Servicio
import com.example.barberialuquitas.util.DURACION_BLOQUE_MINUTOS
import com.google.firebase.firestore.DocumentSnapshot

// Cómo se lee y se guarda cada modelo en Firestore. Los documentos antiguos sin "barberiaId"
// pertenecen a la barbería original (igual que barberiaDe() en firestore.rules).

internal fun DocumentSnapshot.barberiaIdONull(): String? = getString("barberiaId")?.takeIf { it.isNotBlank() }

internal fun aBarberia(documento: DocumentSnapshot) = Barberia(
    id = documento.id,
    nombre = documento.texto("nombre"),
    direccion = documento.texto("direccion"),
    telefono = documento.texto("telefono"),
    fotoUrl = documento.textoOpcional("fotoUrl"),
    activa = documento.getBoolean("activa") ?: false,
    horaApertura = documento.texto("horaApertura", "09:00"),
    horaCierre = documento.texto("horaCierre", "20:00"),
    diasLaborables = documento.listaDeEnteros("diasLaborables"),
    diasNoLaborables = documento.listaDeTextos("diasNoLaborables").sorted(),
    orden = documento.getLong("orden")?.toInt() ?: ORDEN_POR_DEFECTO,
    migrada = documento.getBoolean("migrada") ?: true
)

internal fun Barberia.aMapa(): Map<String, Any?> = mapOf(
    "nombre" to nombre.trim(),
    "direccion" to direccion.trim(),
    "telefono" to telefono.trim(),
    "fotoUrl" to fotoUrl?.trim().orEmpty(),
    "activa" to activa,
    "horaApertura" to horaApertura,
    "horaCierre" to horaCierre,
    "diasLaborables" to diasLaborables.sorted(),
    "diasNoLaborables" to diasNoLaborables.distinct().sorted()
)

internal fun aBarbero(documento: DocumentSnapshot) = Barbero(
    id = documento.id,
    barberiaId = documento.barberiaIdONull() ?: ID_BARBERIA_ORIGINAL,
    nombre = documento.texto("nombre"),
    especialidad = documento.texto("especialidad"),
    fotoUrl = documento.textoOpcional("fotoUrl"),
    activo = documento.getBoolean("activo") ?: false,
    horaInicio = documento.texto("horaInicio", "09:00"),
    horaFin = documento.texto("horaFin", "20:00"),
    diasLaborables = documento.listaDeEnteros("diasLaborables")
)

internal fun Barbero.aMapa(): Map<String, Any?> = mapOf(
    "barberiaId" to barberiaId,
    "nombre" to nombre.trim(),
    "especialidad" to especialidad.trim(),
    "fotoUrl" to fotoUrl?.trim().orEmpty(),
    "activo" to activo,
    "horaInicio" to horaInicio,
    "horaFin" to horaFin,
    "diasLaborables" to diasLaborables.sorted()
)

internal fun aServicio(documento: DocumentSnapshot) = Servicio(
    id = documento.id,
    barberiaId = documento.barberiaIdONull() ?: ID_BARBERIA_ORIGINAL,
    nombre = documento.texto("nombre"),
    descripcion = documento.texto("descripcion"),
    precio = documento.getLong("precio") ?: 0,
    duracionMinutos = documento.getLong("duracionMinutos")?.toInt() ?: DURACION_BLOQUE_MINUTOS,
    fotoUrl = documento.textoOpcional("fotoUrl"),
    activo = documento.getBoolean("activo") ?: true
)

internal fun Servicio.aMapa(): Map<String, Any?> = mapOf(
    "barberiaId" to barberiaId,
    "nombre" to nombre.trim(),
    "descripcion" to descripcion.trim(),
    "precio" to precio,
    "duracionMinutos" to duracionMinutos,
    "fotoUrl" to fotoUrl?.trim().orEmpty(),
    "activo" to activo
)

// Las citas creadas antes de guardar "bloques" ocupaban un solo bloque de 30 minutos.
internal fun aCita(documento: DocumentSnapshot) = Cita(
    id = documento.id,
    barberiaId = documento.barberiaIdONull() ?: ID_BARBERIA_ORIGINAL,
    barberiaNombre = documento.texto("barberiaNombre"),
    clienteId = documento.texto("clienteId"),
    clienteNombre = documento.texto("clienteNombre"),
    servicioId = documento.texto("servicioId"),
    servicioNombre = documento.texto("servicioNombre"),
    precio = documento.getLong("precio") ?: 0,
    barberoId = documento.texto("barberoId"),
    barberoNombre = documento.texto("barberoNombre"),
    fecha = documento.texto("fecha"),
    hora = documento.texto("hora"),
    duracionMinutos = documento.getLong("duracionMinutos")?.toInt() ?: DURACION_BLOQUE_MINUTOS,
    bloques = documento.listaDeTextos("bloques").takeIf { it.isNotEmpty() } ?: listOfNotNull(documento.getString("hora")),
    estado = documento.texto("estado", EstadosCita.RESERVADA)
)
