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
    } catch (e: FirebaseAuthUserCollisionException) {
        Result.failure(Exception("Ese correo ya está registrado."))
    } catch (e: Exception) {
        Result.failure(Exception("No se pudo completar el registro. Intenta nuevamente."))
    }

    suspend fun iniciarSesion(correo: String, password: String): Result<Usuario> = try {
        val resultado = auth.signInWithEmailAndPassword(correo, password).await()
        val uid = resultado.user?.uid ?: error("No se pudo obtener el usuario")

        val documento = firestore.collection(COLECCION_USUARIOS).document(uid).get().await()
        val usuario = Usuario(
            uid = uid,
            nombre = documento.getString("nombre") ?: "",
            correo = documento.getString("correo") ?: correo,
            telefono = documento.getString("telefono") ?: "",
            rol = documento.getString("rol") ?: "cliente"
        )

        Result.success(usuario)
    } catch (e: FirebaseAuthInvalidUserException) {
        Result.failure(Exception("Correo o contraseña incorrectos."))
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        Result.failure(Exception("Correo o contraseña incorrectos."))
    } catch (e: Exception) {
        Result.failure(Exception("No se pudo iniciar sesión. Intenta nuevamente."))
    }

    suspend fun enviarCorreoRecuperacion(correo: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(correo).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception("No se pudo enviar el correo de recuperación."))
    }

    fun cerrarSesion() = auth.signOut()
}
