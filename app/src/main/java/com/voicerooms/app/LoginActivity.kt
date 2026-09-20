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

        loginButton?.setOnClickListener {
            val phoneNumber = phoneInput?.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                Toast.makeText(this, "Sending code...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
