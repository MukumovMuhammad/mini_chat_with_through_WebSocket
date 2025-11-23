package com.example.mini_chat_test.websocket

import AppWebSocketListener
import android.content.Context
import android.util.Log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mini_chat_test.DataClasses.MessageData
import com.example.mini_chat_test.DataClasses.UserDataResponse
import com.example.mini_chat_test.DataClasses.WebSocketSendingData

import com.example.mini_chat_test.saveUsernameAndId
import com.example.mini_chat_test.showNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class ChatViewModel: ViewModel() {

    private val serverUrl = "https://mini-chat-service-1091763228160.europe-west1.run.app/"
    private val okHttpClient = OkHttpClient()

    var context: Context? = null

    private val _status = MutableStateFlow("Disconnected")
    val status: StateFlow<String> = _status

    private val _login_status = MutableStateFlow("not logged")
    val login_status: StateFlow<String> = _login_status


    private val _UserMessages = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val UserMessages: StateFlow<Map<Int, List<String>>> = _UserMessages

    private val _userlist = MutableStateFlow<List<Pair<String,Int>>>(emptyList())
    val userlist: StateFlow<List<Pair<String,Int>>> = _userlist


    var SelectedUSerID : Int? = null

    init {
        // Observe the flows from the singleton WebSocketManager
        observeWebSocket()
    }

//////////////// SIGN UP/LOGIN/LOGOUT/////////////////////
    fun login(username: String, password: String){

        Log.i("ChatViewModel_TAG", "Trying to login with username: $username and password: $password")

    _login_status.value = "Connecting"

        val json = """
                {
                    "username": "${username}",
                    "password": "${password}"
                }
            """.trimIndent()

        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())


        val request = Request.Builder()
            .url(serverUrl+"login")
            .post(requestBody)
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ChatViewModel_TAG", "Error on login: ${e.message}")
                _login_status.value = "failed"
            }

            override fun onResponse(call: Call, response: Response) {
                val json = Json { ignoreUnknownKeys = true }
                val result = response.body?.string()?.let { json.decodeFromString<UserDataResponse>(it) }
                Log.i("ChatViewModel_TAG", "Got the response!")
                Log.i("ChatViewModel_TAG", "Login response: ${result}")

                if (response.code != 200) {
                    Log.e("ChatViewModel_TAG", "Login failed with code: ${response.code}")
                    _login_status.value = "failed"
                }
                else{

                    if(result?.status == true){
                        Log.i("ChatViewModel_TAG", "Login success")
                        Log.i("ChatViewModel_TAG", "id: ${result?.id}")
                        saveUsernameAndId(context!!, username, result?.id!!)
                        WebSocketManager.startConnection(result.id)
                        _login_status.value = "success"
                    }
                    else{
                        _login_status.value = "failed"
                    }




                }

            }
        })

    }

    fun SignUp(username: String, password: String){

        Log.i("ChatViewModel_TAG", "Trying to Sign Up with username: $username and password: $password")


        _login_status.value = "Connecting"

        val json = """
                {
                    "username": "${username}",
                    "password": "${password}"
                }
            """.trimIndent()

        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())


        val request = Request.Builder()
            .url(serverUrl+"sign_up")
            .post(requestBody)
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ChatViewModel_TAG", "Error on sign up: ${e.message}")
                _login_status.value = "failed"
            }

            override fun onResponse(call: Call, response: Response) {
                val json = Json { ignoreUnknownKeys = true }
                val result = response.body?.string()?.let { json.decodeFromString<UserDataResponse>(it) }
                Log.i("ChatViewModel_TAG", "Got the response!")
                Log.i("ChatViewModel_TAG", "SignUp response: ${result}")

                if (response.code != 200) {
                    Log.e("ChatViewModel_TAG", "sign up failed with code: ${response.code}")
                    _login_status.value = "failed"
                }
                else{

                    if(result?.status == true){
                        Log.i("ChatViewModel_TAG", "Login success")
                        _login_status.value = "success"
                        Log.i("ChatViewModel_TAG", "id: ${result?.id}")
                        saveUsernameAndId(context!!, username, result?.id!!)
                        saveUsernameAndId(context!!, username, result?.id!!)
                        WebSocketManager.startConnection(result.id)
                        _login_status.value = "success"
                    }
                    else{
                        _login_status.value = "failed"
                    }


                }

            }
        })

    }

    fun getUsers() {
        val request = Request.Builder()
            .url(serverUrl + "all_users")
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Failed: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                val body: String? = response.body?.string()
                println("Response: $body")

                val root = Json.parseToJsonElement(body!!).jsonArray

                val pairs: List<Pair<String, Int>> = root.map { item ->
                    val arr = item.jsonArray
                    arr[0].jsonPrimitive.content to arr[1].jsonPrimitive.int
                }

                _userlist.value = pairs
            }
        })
    }

    fun LogOut(){
        saveUsernameAndId(context!!, "", null)
        WebSocketManager.closeConnection()
        _status.value = "Disconnected"
    }



    //////////////// WebSockets /////////////////////
    private fun observeWebSocket() {
        // This part is correct. It just listens for whatever the manager is doing.
        viewModelScope.launch {
            WebSocketManager.connectionStatus.collect { newStatus ->
                Log.i("Status_changed_TAG", "The status has been changed to $newStatus")
                _status.value = newStatus
            }
        }

        viewModelScope.launch {
            WebSocketManager.messages.collect { message ->

                if (message?.contains("\"type\":\"ping\"") ?: false){
                    Log.i("WebSocketPing", "This is a ping from the server")
                    WebSocketManager.sendMessage("\"type\":\"ping\"")
                }
                else{
                    // ... your message handling logic here is fine ...
                    if (message != null) {
                        Log.i("Received Message TAG", "We received a message! $message")
                        val result = Json.decodeFromString<MessageData>(message)
                        val currentMessagesForUser = _UserMessages.value[result.from] ?: emptyList()
                        val updatedMessagesForUser = currentMessagesForUser + "${result.username}: ${result.text}"
                        _UserMessages.value = _UserMessages.value + (result.from to updatedMessagesForUser)

                        if (SelectedUSerID != result.from){
                            showNotification(context!!, result.username, result.text)
                        }
                    }
                }

            }
        }
    }

    override fun onCleared() {
        // DO NOT disconnect here anymore. The connection should persist.
        super.onCleared()
        Log.i("ChatViewModel", "ViewModel is cleared, but WebSocket connection remains active.")
    }


    fun sendMessage(reciever_id: Int, message: String) {
        if (message.isNotBlank()) {
            val jsonConverter = Json
            val data = WebSocketSendingData(reciever_id.toString(), message)
            val jsonString = jsonConverter.encodeToString(data)

            // Send message through the manager
            WebSocketManager.sendMessage(jsonString)

            // Update local UI state immediately (optimistic update)
            val currentMessagesForUser = _UserMessages.value[reciever_id] ?: emptyList()
            val updatedMessagesForUser = currentMessagesForUser + "You: ${message}"
            _UserMessages.value = _UserMessages.value + (reciever_id to updatedMessagesForUser)
        }
    }







}