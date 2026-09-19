package com.voicerooms.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.voicerooms.app.data.AppUser
import com.voicerooms.app.data.AuthManager
import com.voicerooms.app.ui.LoginScreen
import com.voicerooms.app.ui.VoiceRoomsApp
import com.voicerooms.app.ui.theme.VoiceRoomsTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            initAgora()
        } else {
            Toast.makeText(this, "يحتاج التطبيق إذن المايك للعمل", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndRequestPermissions()

        setContent {
            VoiceRoomsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentUser by remember { mutableStateOf<AppUser?>(null) }
                    var checking by remember { mutableStateOf(true) }
                    val scope = rememberCoroutineScope()

                    LaunchedEffect(Unit) {
                        if (AuthManager.isLoggedIn()) {
                            currentUser = AuthManager.getCurrentUser()
                        }
                        checking = false
                    }

                    when {
                        checking -> {
                            // شاشة تحميل بسيطة
                        }
                        currentUser == null -> {
                            LoginScreen(onLoggedIn = { user ->
                                currentUser = user
                                Toast.makeText(
                                    this@MainActivity,
                                    "مرحباً ${user.displayName}\nID: ${user.uid}",
                                    Toast.LENGTH_LONG
                                ).show()
                            })
                        }
                        else -> {
                            VoiceRoomsApp(
                                currentUser = currentUser!!,
                                onLogout = {
                                    AuthManager.signOut(this@MainActivity)
                                    currentUser = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )
        val needRequest = permissions.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needRequest) {
            permissionLauncher.launch(permissions)
        } else {
            initAgora()
        }
    }

    private fun initAgora() {
        val ok = AgoraVoiceManager.initialize(this)
        if (!ok) {
            Toast.makeText(this, "فشل تهيئة محرك الصوت", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AgoraVoiceManager.destroy()
    }
}
