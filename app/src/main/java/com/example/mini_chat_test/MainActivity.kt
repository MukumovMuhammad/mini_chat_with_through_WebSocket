package com.example.mini_chat_test

import android.os.Bundle
import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.unit.dp
import com.example.mini_chat_test.ui.theme.Mini_chat_testTheme
import com.example.mini_chat_test.websocket.ChatViewModel
import com.example.mini_chat_test.websocket.WebSocketManager


private var TAG = "MAinActivity_TAG"


class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    val selectedId = mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        viewModel.context = this

        var savedId: Int? = getSavedId(this)

        if(savedId != null){
            Log.i(TAG, "Trying to connect the Socket id : ${savedId}")
            WebSocketManager.startConnection(savedId)
        }

        setContent {
            val status by viewModel.login_status.collectAsState()
            if (selectedId.value != null)  viewModel.SelectedUSerID = selectedId.value
            Mini_chat_testTheme {
                if (status != "success"){
                    LoginScreen(viewModel)
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

                        if(users != null) {
                            UserListScreen(viewModel, users) { userId ->
                                selectedId.value = userId
                                Log.i(TAG, "Selected user ID: $userId")
                            }
                        }
                        else{
                            Text("It seems no one has registered yet :(")
                        }

                    }
                }
            }
        }

    }
}


@Composable
fun LoginScreen(viewModel: ChatViewModel) {


    var inputUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isloading by remember {mutableStateOf(false)}
    val status by viewModel.login_status.collectAsState()

    isloading = status == "Connecting"
    LoadingDialog(isloading, "connecting")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Status: $status",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

//        Username
        OutlinedTextField(
            value = inputUsername,
            onValueChange = { inputUsername = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

//        Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                Log.i(TAG, "Trying to login!")
                viewModel.login(inputUsername, password)
            }
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                Log.i(TAG, "Trying to Sign Up!")
                viewModel.SignUp(inputUsername, password)
            }
        ) {
            Text("Sign Up")
        }
    }
}
