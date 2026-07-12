package com.example.data.firebase

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUser get() = auth.currentUser

    suspend fun signInWithEmail(email: String, pass: String): Result<Unit> {
        return try {
            val authResult = kotlinx.coroutines.withTimeout(10000) {
                auth.signInWithEmailAndPassword(email, pass).await()
            }
            val user = authResult.user
            if (user != null) {
                val userMap = hashMapOf(
                    "uid" to user.uid,
                    "email" to user.email
                )
                db.collection("users").document(user.uid).set(userMap)
            }
            Result.success(Unit)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("انتهى وقت الاتصال. يرجى المحاولة مرة أخرى."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String): Result<Unit> {
        return try {
            val authResult = kotlinx.coroutines.withTimeout(10000) {
                auth.createUserWithEmailAndPassword(email, pass).await()
            }
            val user = authResult.user
            if (user != null) {
                val userMap = hashMapOf(
                    "uid" to user.uid,
                    "email" to user.email
                )
                db.collection("users").document(user.uid).set(userMap)
            }
            Result.success(Unit)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("انتهى وقت الاتصال. يرجى المحاولة مرة أخرى."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = kotlinx.coroutines.withTimeout(10000) {
                auth.signInWithCredential(firebaseCredential).await()
            }
            
            val user = authResult.user
            if (user != null) {
                val userMap = hashMapOf(
                    "uid" to user.uid,
                    "email" to user.email,
                    "displayName" to user.displayName,
                    "photoUrl" to user.photoUrl?.toString()
                )
                db.collection("users").document(user.uid).set(userMap)
            }
            Result.success(Unit)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("انتهى وقت الاتصال. يرجى المحاولة مرة أخرى."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<Unit> {
        return try {
            val authResult = kotlinx.coroutines.withTimeout(10000) {
                auth.signInAnonymously().await()
            }
            val user = authResult.user
            if (user != null) {
                val userMap = hashMapOf(
                    "uid" to user.uid,
                    "isAnonymous" to true
                )
                db.collection("users").document(user.uid).set(userMap)
            }
            Result.success(Unit)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("انتهى وقت الاتصال. يرجى المحاولة مرة أخرى."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
