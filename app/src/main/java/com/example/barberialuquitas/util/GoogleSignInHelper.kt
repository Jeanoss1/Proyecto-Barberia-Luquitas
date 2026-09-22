package com.example.barberialuquitas.util

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

private const val GOOGLE_WEB_CLIENT_ID =
    "238705139924-o7s7o7v1tddrbh4ul3k8dq5obkeegf92.apps.googleusercontent.com"

suspend fun obtenerGoogleIdToken(context: Context): String {
    val credentialManager = CredentialManager.create(context)
    val opcion = GetSignInWithGoogleOption.Builder(GOOGLE_WEB_CLIENT_ID).build()
    val solicitud = GetCredentialRequest.Builder().addCredentialOption(opcion).build()
    val resultado = credentialManager.getCredential(context, solicitud)
    val credencial = GoogleIdTokenCredential.createFrom(resultado.credential.data)
    return credencial.idToken
}
