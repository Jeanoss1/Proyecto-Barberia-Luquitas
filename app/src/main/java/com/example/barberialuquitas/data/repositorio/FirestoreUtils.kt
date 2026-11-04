package com.example.barberialuquitas.data.repositorio

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// Escucha una consulta en tiempo real. Si falla, emite un error con un mensaje para el usuario y se cierra
// (la pantalla vuelve a suscribirse al pulsar "Reintentar").
internal fun <T> Query.observar(
    tag: String,
    mensajeError: String,
    convertir: (DocumentSnapshot) -> T
): Flow<Result<List<T>>> = callbackFlow {
    val registro = addSnapshotListener { snapshot, error ->
        if (error != null) {
            Log.e(tag, mensajeError, error)
            trySend(Result.failure(Exception(mensajeError)))
            close()
            return@addSnapshotListener
        }
        trySend(Result.success(snapshot?.documents.orEmpty().map(convertir)))
    }
    awaitClose { registro.remove() }
}

// Igual que Query.observar pero para un solo documento; emite null si no existe.
internal fun <T> DocumentReference.observar(
    tag: String,
    mensajeError: String,
    convertir: (DocumentSnapshot) -> T
): Flow<Result<T?>> = callbackFlow {
    val registro = addSnapshotListener { snapshot, error ->
        if (error != null) {
            Log.e(tag, mensajeError, error)
            trySend(Result.failure(Exception(mensajeError)))
            close()
            return@addSnapshotListener
        }
        trySend(Result.success(snapshot?.takeIf { it.exists() }?.let(convertir)))
    }
    awaitClose { registro.remove() }
}

// Ejecuta una operación y convierte cualquier excepción en un Result con un mensaje legible.
internal inline fun <T> intentar(tag: String, mensajeError: String, operacion: () -> T): Result<T> = try {
    Result.success(operacion())
} catch (e: Exception) {
    Log.e(tag, mensajeError, e)
    Result.failure(Exception(mensajeError))
}

internal fun DocumentSnapshot.texto(campo: String, porDefecto: String = ""): String = getString(campo) ?: porDefecto

internal fun DocumentSnapshot.textoOpcional(campo: String): String? = getString(campo)?.takeIf { it.isNotBlank() }

internal fun DocumentSnapshot.listaDeEnteros(campo: String): List<Int> =
    (get(campo) as? List<*>).orEmpty().mapNotNull { (it as? Number)?.toInt() }

internal fun DocumentSnapshot.listaDeTextos(campo: String): List<String> =
    (get(campo) as? List<*>).orEmpty().filterIsInstance<String>()
