package com.voicerooms.app.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

object AuthManager {

    private const val TAG = "AuthManager"
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun currentUid(): String? = auth.currentUser?.uid
    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    suspend fun handleGoogleSignInResult(data: Intent?): Result<AppUser> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("User null"))
            val appUser = getOrCreateUser(
                uid = firebaseUser.uid,
                name = firebaseUser.displayName ?: account.displayName ?: "مستخدم",
                email = firebaseUser.email ?: account.email ?: "",
                photo = firebaseUser.photoUrl?.toString() ?: ""
            )
            Result.success(appUser)
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed", e)
            Result.failure(e)
        }
    }

    private var verificationId: String? = null

    fun startPhoneVerification(
        phone: String,
        activity: Activity,
        onCodeSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                override fun onVerificationFailed(e: FirebaseException) {
                    onError(e.message ?: "فشل التحقق")
                }
                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationId = id
                    onCodeSent()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyPhoneCode(code: String, name: String = "مستخدم"): Result<AppUser> {
        return try {
            val id = verificationId ?: return Result.failure(Exception("لم يتم إرسال كود"))
            val credential = PhoneAuthProvider.getCredential(id, code)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(Exception("User null"))
            val appUser = getOrCreateUser(
                uid = user.uid,
                name = name,
                email = "",
                photo = "",
                phone = user.phoneNumber ?: ""
            )
            Result.success(appUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(displayName: String): Result<AppUser> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user ?: return Result.failure(Exception("User null"))
            val appUser = getOrCreateUser(
                uid = user.uid,
                name = displayName.ifBlank { "ضيف" },
                email = "",
                photo = ""
            )
            Result.success(appUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getOrCreateUser(
        uid: String,
        name: String,
        email: String,
        photo: String,
        phone: String = ""
    ): AppUser {
        val doc = db.collection("users").document(uid).get().await()
        if (doc.exists()) {
            return doc.toAppUser()
        }
        val newUser = AppUser(
            uid = uid,
            displayName = name,
            email = email,
            phone = phone,
            photoUrl = photo,
            role = UserRole.USER,
            coins = 100
        )
        db.collection("users").document(uid).set(newUser.toMap()).await()
        return newUser
    }

    suspend fun getCurrentUser(): AppUser? {
        val uid = currentUid() ?: return null
        val doc = db.collection("users").document(uid).get().await()
        return if (doc.exists()) doc.toAppUser() else null
    }

    suspend fun updateUserRole(uid: String, role: UserRole): Boolean {
        return try {
            db.collection("users").document(uid).update("role", role.name).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "update role failed", e)
            false
        }
    }

    suspend fun setSuperAdmin(uid: String): Boolean = updateUserRole(uid, UserRole.SUPER_ADMIN)

    fun signOut(context: Context) {
        auth.signOut()
        try { getGoogleSignInClient(context).signOut() } catch (_: Exception) {}
    }

    private fun AppUser.toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "displayName" to displayName,
        "email" to email,
        "phone" to phone,
        "photoUrl" to photoUrl,
        "role" to role.name,
        "coins" to coins,
        "isVip" to isVip,
        "vipLevel" to vipLevel,
        "createdAt" to createdAt
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toAppUser(): AppUser {
        return AppUser(
            uid = getString("uid") ?: id,
            displayName = getString("displayName") ?: "",
            email = getString("email") ?: "",
            phone = getString("phone") ?: "",
            photoUrl = getString("photoUrl") ?: "",
            role = try {
                UserRole.valueOf(getString("role") ?: "USER")
            } catch (_: Exception) {
                UserRole.USER
            },
            coins = (getLong("coins") ?: 0L).toInt(),
            isVip = getBoolean("isVip") ?: false,
            vipLevel = (getLong("vipLevel") ?: 0L).toInt(),
            createdAt = getLong("createdAt") ?: 0L
        )
    }
}
