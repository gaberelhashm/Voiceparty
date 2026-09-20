package com.voicerooms.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etPhone: EditText
    private lateinit var etCode: EditText
    private lateinit var btnSendCode: Button
    private lateinit var btnVerify: Button
    private var verificationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)

        etPhone = findViewById(R.id.etPhone)
        etCode = findViewById(R.id.etCode)
        btnSendCode = findViewById(R.id.btnSendCode)
        btnVerify = findViewById(R.id.btnVerify)

        btnSendCode.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            if (phone.isNotEmpty()) {
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phone)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            auth.signInWithCredential(credential).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Toast.makeText(baseContext, "تم تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            Toast.makeText(baseContext, "فشل التحقق: ${e.message}", Toast.DEFAULT).show()
                        }

                        override fun onCodeSent(vId: String, token: PhoneAuthProvider.ForceResendingToken) {
                            super.onCodeSent(vId, token)
                            verificationId = vId
                            Toast.makeText(baseContext, "تم إرسال الكود", Toast.LENGTH_SHORT).show()
                        }
                    }).build()
  cd ~/VindorVoiceNew/VindorVoice-vindorvoice-30
cat << 'EOF' > app/src/main/java/com/voicerooms/app/LoginActivity.kt
package com.voicerooms.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val phoneInput = findViewById<EditText>(R.id.etPhone)
        val loginButton = findViewById<Button>(R.id.btnLogin)

        loginButton.setOnClickListener {
            val phoneNumber = phoneInput.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                Toast.makeText(this, "Sending verification code...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
EOF

