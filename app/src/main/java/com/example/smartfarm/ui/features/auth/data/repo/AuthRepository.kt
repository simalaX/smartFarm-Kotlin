package com.example.smartfarm.ui.features.auth.data.repo

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.example.smartfarm.activity.BuildConfig  // Changed this import
import com.example.smartfarm.ui.features.auth.data.local.UserPreferences
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userPreferences: UserPreferences
) {
    private fun getCredentialManager(context: Context): CredentialManager {
        return CredentialManager.create(context)
    }

    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser
    val isLoggedIn = userPreferences.isLoggedIn

    suspend fun signInWithGoogle(context: Context): AuthResult {
        return try {
            val credentialResponse = getGoogleCredentials(context)
            val googleIdToken = extractGoogleIdToken(credentialResponse)
                ?: return AuthResult.Error("Failed to extract Google ID token")
            authenticateWithFirebase(googleIdToken)
        } catch (e: GetCredentialException) {
            Timber.tag(TAG).e(e, "Credential Manager error")
            AuthResult.Error(handleCredentialException(e))
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Sign-in error")
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    private suspend fun getGoogleCredentials(context: Context): GetCredentialResponse {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return getCredentialManager(context).getCredential(
            request = request,
            context = context
        )
    }

    private fun extractGoogleIdToken(response: GetCredentialResponse): String? {
        val credential = response.credential
        return if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            googleIdTokenCredential.idToken
        } else {
            null
        }
    }

    private suspend fun authenticateWithFirebase(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user

            if (user != null) {
                userPreferences.saveUserSession(
                    userId = user.uid,
                    email = user.email ?: "",
                    name = user.displayName ?: ""
                )
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Authentication failed: User is null")
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Firebase auth error")
            AuthResult.Error(e.message ?: "Firebase authentication failed")
        }
    }

    suspend fun signOut(context: Context) {
        try {
            firebaseAuth.signOut()
            userPreferences.clearUserSession()
            val clearRequest = ClearCredentialStateRequest()
            getCredentialManager(context).clearCredentialState(clearRequest)
            Timber.tag(TAG).d("Sign out successful")
        } catch (e: ClearCredentialException) {
            Timber.tag(TAG).e(e, "Error clearing credentials")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Sign out error")
        }
    }

    private fun handleCredentialException(e: GetCredentialException): String {
        return when (e::class.simpleName) {
            "GetCredentialCancellationException" -> "Sign-in was cancelled"
            "GetCredentialInterruptedException" -> "Sign-in was interrupted"
            "NoCredentialException" -> "No Google accounts found. Please add a Google account."
            "GetCredentialUnknownException" -> "An unknown error occurred"
            else -> e.message ?: "Failed to sign in with Google"
        }
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}