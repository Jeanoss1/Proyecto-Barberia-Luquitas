package com.example.barberialuquitas.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val COLECCION_USUARIOS = "usuarios"

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun registrar(
        nombre: String,
        correo: String,
        telefono: String,
        password: String
    ): Result<Usuario> = try {
        val resultado = auth.createUserWithEmailAndPassword(correo, password).await()
        val uid = resultado.user?.uid ?: error("No se pudo obtener el usuario creado")
        val usuario = Usuario(uid = uid, nombre = nombre, correo = correo, telefono = telefono)

        firestore.collection(COLECCION_USUARIOS).document(uid)
            .set(
                mapOf(
                    "nombre" to usuario.nombre,
                    "correo" to usuario.correo,
                    "telefono" to usuario.telefono,
                    "rol" to usuario.rol
                )
            ).await()

        Result.success(usuario)
    } catch (_: FirebaseAuthUserCollisionException) {
        Result.failure(Exception("Ese correo ya está registrado."))
    } catch (_: Exception) {
        Result.failure(Exception("No se pudo completar el registro. Intenta nuevamente."))
    }

    suspend fun iniciarSesion(correo: String, password: String): Result<Usuario> = try {
        val resultado = auth.signInWithEmailAndPassword(correo, password).await()
        val uid = resultado.user?.uid ?: error("No se pudo obtener el usuario")
        Result.success(obtenerUsuario(uid, correo))
    } catch (_: FirebaseAuthInvalidUserException) {
        Result.failure(Exception("Correo o contraseña incorrectos."))
    } catch (_: FirebaseAuthInvalidCredentialsException) {
        Result.failure(Exception("Correo o contraseña incorrectos."))
    } catch (_: Exception) {
        Result.failure(Exception("No se pudo iniciar sesión. Intenta nuevamente."))
    }

    suspend fun enviarCorreoRecuperacion(correo: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(correo).await()
        Result.success(Unit)
    } catch (_: Exception) {
        Result.failure(Exception("No se pudo enviar el correo de recuperación."))
    }

    suspend fun obtenerUsuarioActual(): Result<Usuario> = try {
        val uid = auth.currentUser?.uid ?: error("No hay una sesión activa")
        Result.success(obtenerUsuario(uid, auth.currentUser?.email ?: ""))
    } catch (_: Exception) {
        Result.failure(Exception("No se pudo cargar el perfil."))
    }

    fun cerrarSesion() = auth.signOut()

    private suspend fun obtenerUsuario(uid: String, correoFallback: String): Usuario {
        val documento = firestore.collection(COLECCION_USUARIOS).document(uid).get().await()
        return Usuario(
            uid = uid,
            nombre = documento.getString("nombre") ?: "",
            correo = documento.getString("correo") ?: correoFallback,
            telefono = documento.getString("telefono") ?: "",
            rol = documento.getString("rol") ?: "cliente"
        )
    }
}
