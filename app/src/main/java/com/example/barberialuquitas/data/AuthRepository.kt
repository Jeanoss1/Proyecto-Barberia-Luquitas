package com.example.barberialuquitas.data

import android.util.Log
import com.example.barberialuquitas.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val COLECCION_USUARIOS = "usuarios"
private const val TAG = "AuthRepository"

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    init {
        if (BuildConfig.DEBUG) {
            // Los emuladores sin Play Store certificada no pueden completar la
            // verificacion de reCAPTCHA que Firebase exige por defecto en signup/login.
            auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        }
    }

    suspend fun registrar(
        nombre: String,
        correo: String,
        telefono: String,
        password: String
    ): Result<Usuario> = try {
        val resultado = auth.createUserWithEmailAndPassword(correo, password).await()
        val uid = resultado.user?.uid ?: error("No se pudo obtener el usuario creado")
        val usuario = Usuario(uid = uid, nombre = nombre, correo = correo, telefono = telefono)
        guardarUsuario(usuario)
        Result.success(usuario)
    } catch (e: FirebaseAuthUserCollisionException) {
        Log.e(TAG, "registrar: correo ya registrado", e)
        Result.failure(Exception("Ese correo ya está registrado."))
    } catch (e: Exception) {
        Log.e(TAG, "registrar: fallo inesperado", e)
        // TEMPORAL: muestra el mensaje real mientras depuramos. Revertir después.
        Result.failure(Exception("Error al registrar: ${e.message ?: e.javaClass.simpleName}"))
    }

    suspend fun iniciarSesion(correo: String, password: String): Result<Usuario> = try {
        val resultado = auth.signInWithEmailAndPassword(correo, password).await()
        val uid = resultado.user?.uid ?: error("No se pudo obtener el usuario")
        Result.success(obtenerUsuario(uid, correo))
    } catch (e: FirebaseAuthInvalidUserException) {
        Log.e(TAG, "iniciarSesion: usuario invalido", e)
        Result.failure(Exception("Correo o contraseña incorrectos."))
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        Log.e(TAG, "iniciarSesion: credenciales invalidas", e)
        Result.failure(Exception("Correo o contraseña incorrectos."))
    } catch (e: Exception) {
        Log.e(TAG, "iniciarSesion: fallo inesperado", e)
        // TEMPORAL: muestra el mensaje real mientras depuramos. Revertir después.
        Result.failure(Exception("Error al iniciar sesión: ${e.message ?: e.javaClass.simpleName}"))
    }

    suspend fun iniciarSesionConGoogle(idToken: String): Result<Usuario> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val resultado = auth.signInWithCredential(credential).await()
        val user = resultado.user ?: error("No se pudo obtener el usuario")
        val documento = firestore.collection(COLECCION_USUARIOS).document(user.uid).get().await()

        val usuario = if (documento.exists()) {
            obtenerUsuario(user.uid, user.email ?: "")
        } else {
            val nuevo = Usuario(
                uid = user.uid,
                nombre = user.displayName ?: "",
                correo = user.email ?: "",
                telefono = ""
            )
            guardarUsuario(nuevo)
            nuevo
        }

        Result.success(usuario)
    } catch (e: Exception) {
        Log.e(TAG, "iniciarSesionConGoogle: fallo inesperado", e)
        // TEMPORAL: muestra el mensaje real mientras depuramos. Revertir después.
        Result.failure(Exception("Error al iniciar sesión con Google: ${e.message ?: e.javaClass.simpleName}"))
    }

    suspend fun enviarCorreoRecuperacion(correo: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(correo).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "enviarCorreoRecuperacion: fallo inesperado", e)
        Result.failure(Exception("No se pudo enviar el correo de recuperación."))
    }

    suspend fun obtenerUsuarioActual(): Result<Usuario> = try {
        val uid = auth.currentUser?.uid ?: error("No hay una sesión activa")
        Result.success(obtenerUsuario(uid, auth.currentUser?.email ?: ""))
    } catch (e: Exception) {
        Log.e(TAG, "obtenerUsuarioActual: fallo inesperado", e)
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

    private suspend fun guardarUsuario(usuario: Usuario) {
        firestore.collection(COLECCION_USUARIOS).document(usuario.uid)
            .set(
                mapOf(
                    "nombre" to usuario.nombre,
                    "correo" to usuario.correo,
                    "telefono" to usuario.telefono,
                    "rol" to usuario.rol
                )
            ).await()
    }
}