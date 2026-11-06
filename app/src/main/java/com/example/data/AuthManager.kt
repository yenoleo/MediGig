package com.example.data

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object AuthManager {

  private const val TAG = "MediGigAuth"

  private val _currentUser = MutableStateFlow<FirebaseUser?>(Firebase.auth.currentUser)
  val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _authError = MutableStateFlow<String?>(null)
  val authError: StateFlow<String?> = _authError.asStateFlow()

  init {
    Firebase.auth.addAuthStateListener { auth ->
      _currentUser.value = auth.currentUser
    }
  }

  fun attemptAutoSignIn(
    context: Context,
    scope: CoroutineScope
  ) {
    if (Firebase.auth.currentUser != null) {
      _currentUser.value = Firebase.auth.currentUser
      return
    }

    val clientId = try {
      context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
      Log.w(TAG, "default_web_client_id not available for auto-signin", e)
      return
    }

    val credentialManager = CredentialManager.create(context)
    val googleIdOption = GetGoogleIdOption.Builder()
      .setFilterByAuthorizedAccounts(true)
      .setServerClientId(clientId)
      .setAutoSelectEnabled(true)
      .build()

    val request = GetCredentialRequest.Builder()
      .addCredentialOption(googleIdOption)
      .build()

    scope.launch {
      try {
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
          val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
          val authResult = Firebase.auth.signInWithCredential(authCredential).await()
          _currentUser.value = authResult.user
        }
      } catch (e: Exception) {
        Log.d(TAG, "Silent auto-sign-in skipped (no saved credentials)")
      }
    }
  }

  fun signInWithGoogle(
    activity: Activity,
    scope: CoroutineScope,
    onSuccess: () -> Unit = {},
    onError: (String) -> Unit = {}
  ) {
    val clientId = try {
      activity.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
      val err = "Google Sign-In configuration missing: default_web_client_id not found"
      _authError.value = err
      onError(err)
      return
    }

    _isLoading.value = true
    _authError.value = null

    val credentialManager = CredentialManager.create(activity)
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
      try {
        val result = credentialManager.getCredential(activity, request)
        val credential = result.credential
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
          val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
          val authResult = Firebase.auth.signInWithCredential(authCredential).await()
          _currentUser.value = authResult.user
          _isLoading.value = false
          onSuccess()
        } else {
          val err = "Unexpected credential type returned"
          _authError.value = err
          _isLoading.value = false
          onError(err)
        }
      } catch (e: GetCredentialCancellationException) {
        Log.w(TAG, "Google Sign-In flow cancelled: ${e.message}", e)
        _isLoading.value = false
      } catch (e: Exception) {
        Log.e(TAG, "Google Sign-In failed", e)
        val err = e.localizedMessage ?: "Sign in failed"
        _authError.value = err
        _isLoading.value = false
        onError(err)
      }
    }
  }

  fun signOut(
    context: Context,
    scope: CoroutineScope,
    onComplete: () -> Unit = {}
  ) {
    Firebase.auth.signOut()
    _currentUser.value = null
    val credentialManager = CredentialManager.create(context)
    scope.launch {
      try {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
      } catch (e: Exception) {
        Log.e(TAG, "Failed to clear credential state", e)
      } finally {
        onComplete()
      }
    }
  }
}
