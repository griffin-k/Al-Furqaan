package com.my.darkmatter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen(
                onLoginSuccess = {
                    runOnUiThread {
                        val intent = Intent(this, Home::class.java)
                        startActivity(intent)
                        finish()
                    }
                },
                onNavigateToRegister = {
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onNavigateToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            val database = FirebaseDatabase.getInstance().getReference("users")
                            val formattedEmail = email.replace(".", ",")
                            database.child(formattedEmail).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val user = snapshot.getValue(User::class.java)
                                        if (user?.password == password) {
                                            onLoginSuccess()
                                        } else {

                                            runBlocking {
                                                withContext(Dispatchers.Main) {
                                                    errorMessage = "Incorrect password!"
                                                }
                                            }
                                        }
                                    } else {

                                        runBlocking {
                                            withContext(Dispatchers.Main) {
                                                errorMessage = "User not found!"
                                            }
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("LoginError", "Database error: ${error.message}")
                                    runBlocking {
                                        withContext(Dispatchers.Main) {
                                            errorMessage = "Database error: ${error.message}"
                                        }
                                    }
                                }
                            })
                        } else {
                            errorMessage = "Please fill all fields!"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text("Login")
                }

                Button(
                    onClick = { onNavigateToRegister() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Don't have an account? Register")
                }

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

data class User(val username: String = "", val email: String = "", val password: String = "")
