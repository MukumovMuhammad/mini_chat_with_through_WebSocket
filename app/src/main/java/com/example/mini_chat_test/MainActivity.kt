package com.example.mini_chat_test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mini_chat_test.ui.theme.Mini_chat_testTheme
import com.example.mini_chat_test.websocket.ChatViewModel
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()
    val client_id: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            Mini_chat_testTheme {
                loginScreen(viewModel, client_id)
            }
        }
    }
}


@Composable
fun loginScreen(viewModel: ChatViewModel, client_id: Long){
    val status by viewModel.login_status.collectAsState()
    var inputUsername by remember { mutableStateOf("") }

    if (status == "not logged" || status == "failed"){
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = "Status: $status", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))


            OutlinedTextField(
                value = inputUsername,
                onValueChange = { inputUsername = it },
                label = { Text("Enter username") }
            )
            Button(
                onClick = {
                    viewModel.login(inputUsername, client_id)
                }
            ) {
                Text("Send")
            }

        }
    }else{
        ChatScreen(viewModel, client_id)
    }




}

@Composable
fun ChatScreen(viewModel: ChatViewModel, client_id: Long) {
    val messages by viewModel.messages.collectAsState()
    val status by viewModel.status.collectAsState()
    var inputMessage by remember { mutableStateOf("") }


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Status: $status", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(2f).fillMaxWidth().padding(vertical = 10.dp),
            reverseLayout = true // Show latest message at the bottom
        ) {
            items(messages.reversed()) { message ->
                val data = Json.decodeFromString<Message>(message)
                val sender = if (data.client_id == client_id.toString()) "You" else data.client_id

                Text(text = sender + ": " + data.message, modifier = Modifier.padding(2.dp))
            }
        }

        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            OutlinedTextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                modifier = Modifier.weight(1f),
                label = { Text("Enter message") }
            )
            Spacer(modifier = Modifier.width(5.dp))
            Button(
                onClick = {
                    viewModel.sendMessage(inputMessage)
                    inputMessage = ""
                },
                enabled = status == "Connected"
            ) {
                Text("Send")
            }
        }
    }
}
