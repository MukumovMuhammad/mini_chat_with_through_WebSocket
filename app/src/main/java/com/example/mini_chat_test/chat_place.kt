package com.example.mini_chat_test

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mini_chat_test.DataClasses.Message
import com.example.mini_chat_test.websocket.ChatViewModel
import kotlinx.serialization.json.Json


@Composable
fun UserListScreen(
    viewModel: ChatViewModel,
    users: List<Pair<String, Int>>,   // (username, userId)
    onUserSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    Column() {
        Row(modifier = Modifier.fillMaxWidth(),  horizontalArrangement = Arrangement.spacedBy(16.dp)){
            Text(
                text = "Your profile ${getSavedUsername(LocalContext.current)}",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center)
            Button(
                onClick = { viewModel.LogOut(context) },
                modifier = Modifier.padding(16.dp)
            ) { Text("LogOut") }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(users) { user ->
                UserCard(
                    username = user.first,
                    onClick = { onUserSelected(user.second) }
                )
            }
        }
    }

}

@Composable
fun UserCard(
    username: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF4F4F4)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle avatar with first letter
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF6200EE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.first().uppercase(),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = username,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val messages = emptyList<String>()

    var inputMessage by remember { mutableStateOf("") }


    Column(modifier = Modifier.fillMaxSize().padding(16.dp, 8.dp)) {

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.weight(2f).fillMaxWidth().padding(vertical = 10.dp),
            reverseLayout = true // Show latest message at the bottom
        ) {
            items(messages.reversed()) { message ->
//                Log.i(TAG, "Here we have message: $message")

                val data = Json.decodeFromString<Message>(message)
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
            ) {
                Text("Send")
            }
        }
    }
}
