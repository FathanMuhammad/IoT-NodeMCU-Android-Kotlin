package com.fathan.projectpifathan2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
// Import tambahan yang diperlukan 27 Mei
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.CardDefaults
//
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartLampTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: SmartLampViewModel = viewModel()
                    SmartLampApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLampApp(viewModel: SmartLampViewModel) {
    var lampStatus by remember { mutableStateOf("OFF") }
    var isLoading by remember { mutableStateOf(true) }

    // Observe Firebase changes
    LaunchedEffect(key1 = Unit) {
        viewModel.getLampStatus { status ->
            lampStatus = status
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp), //
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4B352A) // Warna HIJAU
            ),

        ) {
            Text(
                text = "Kontrol Lampu pintar Milik Fathan",
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            // Lamp status indicator
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(100.dp),
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Lamp",
                    tint = if (lampStatus == "ON") Color.Yellow else Color.Gray.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Status Lampu: $lampStatus",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Toggle button
            Button(
                onClick = { viewModel.toggleLamp() },
                modifier = Modifier.width(200.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (lampStatus == "ON") MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(text = if (lampStatus == "ON") "TURN OFF" else "TURN ON")
            }
        }
    }
}

class SmartLampViewModel : ViewModel() {
    private val database = Firebase.database
    private val lampRef = database.getReference("trialrelay/Relay1")

    fun getLampStatus(callback: (String) -> Unit) {
        lampRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java) ?: "OFF"
                callback(status)
            }

            override fun onCancelled(error: DatabaseError) {
                callback("OFF")
            }
        })
    }

    fun toggleLamp() {
        lampRef.get().addOnSuccessListener { snapshot ->
            val currentStatus = snapshot.getValue(String::class.java) ?: "OFF"
            val newStatus = if (currentStatus == "ON") "OFF" else "ON"
            lampRef.setValue(newStatus)
        }
    }
}

@Composable
fun SmartLampTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        content = content
    )
}