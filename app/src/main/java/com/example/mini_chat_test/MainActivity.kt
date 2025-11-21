package com.example.mini_chat_test

import android.os.Bundle
import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.unit.dp
import com.example.mini_chat_test.ui.theme.Mini_chat_testTheme
import com.example.mini_chat_test.websocket.ChatViewModel


private var TAG = "MAinActivity_TAG"


class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    val selectedId = mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (getSavedId(this) != null){
            viewModel.WebSocketInit(getSavedId(this)?.toInt())
        }

        setContent {
            val status by viewModel.status.collectAsState()
            Mini_chat_testTheme {
                if (status != "Connected"){
                    loginScreen(viewModel, status)
                }
                else{

                    if (selectedId.value != null) {
                        BackHandler {
                            selectedId.value = null
                        }
                        ChatScreen(viewModel, selectedId.value!!)
                    } else {
                        viewModel.getUsers()
                        val users by viewModel.userlist.collectAsState()

                        if (users != null) {
                            UserListScreen(viewModel, users) { userId ->
                                selectedId.value = userId
                                Log.i(TAG, "Selected user ID: $userId")
                            }
                        }
                    }
                }
            }
        }

    }
}



@Composable
fun loginScreen(viewModel: ChatViewModel, status: String){

    var context = LocalContext.current
    var inputUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Status: $status", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))


        OutlinedTextField(
            value = inputUsername,
            onValueChange = { inputUsername = it },
            label = { Text("Enter username") }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter password") }
        )
        Button(
            onClick = {
                Log.i(TAG, "Trying to login!")
                Log.i(TAG, "Username: $inputUsername")
                Log.i(TAG, "Password: $password")
                viewModel.login(context, inputUsername, password)
            }
        ) {
            Text("Login")
        }

        Button(
            onClick = {
                Log.i(TAG, "Trying to Sign Up!")
                Log.i(TAG, "Username: $inputUsername")
                Log.i(TAG, "Password: $password")
                viewModel.SignUp(context, inputUsername, password)
            }
        ) {
            Text("Sign Up")
        }
    }
}
