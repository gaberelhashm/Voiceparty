package com.voicerooms.app.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voicerooms.app.data.AppUser
import com.voicerooms.app.data.AuthManager
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoggedIn: (AppUser) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var displayName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(0) } // 0=main, 1=phone code
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "VidorVoice",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "غرف صوتية • دردشة • ألعاب",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(40.dp))

            when (step) {
                0 -> {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("اسمك") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (displayName.isBlank()) {
                                error = "اكتب اسمك"
                                return@Button
                            }
                            loading = true
                            error = null
                            scope.launch {
                                val result = AuthManager.signInAnonymously(displayName.trim())
                                loading = false
                                result.onSuccess { onLoggedIn(it) }
                                    .onFailure { error = it.message ?: "فشل الدخول" }
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (loading) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary)
                        else Text("دخول سريع", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("أو", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { step = 1; error = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Phone, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("الدخول برقم التليفون")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ملاحظة: جوجل وفيسبوك يحتاجوا تفعيل إضافي في Firebase",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                1 -> {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم التليفون (مثال +2010...)") },
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("كود التحقق (بعد الإرسال)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val activity = context as? Activity
                            if (activity == null) {
                                error = "خطأ في النشاط"
                                return@Button
                            }
                            if (phone.isBlank()) {
                                error = "أدخل رقم التليفون"
                                return@Button
                            }
                            loading = true
                            error = null
                            AuthManager.startPhoneVerification(
                                phone = phone.trim(),
                                activity = activity,
                                onCodeSent = {
                                    loading = false
                                    error = "تم إرسال الكود، أدخله بالأعلى واضغط تحقق"
                                },
                                onError = {
                                    loading = false
                                    error = it
                                }
                            )
                        },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("إرسال كود")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (code.isBlank()) {
                                error = "أدخل الكود"
                                return@Button
                            }
                            loading = true
                            error = null
                            scope.launch {
                                val result = AuthManager.verifyPhoneCode(code.trim(), displayName.ifBlank { "مستخدم" })
                                loading = false
                                result.onSuccess { onLoggedIn(it) }
                                    .onFailure { error = it.message ?: "كود خاطئ" }
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("تحقق وادخل")
                    }

                    TextButton(onClick = { step = 0; error = null }) {
                        Text("رجوع")
                    }
                }
            }

            error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
        }
    }
}
