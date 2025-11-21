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

    private val serverUrl = "https://simple-chat-by-mm.onrender.com/"
    private val webSocketUrl = "https://simple-chat-by-mm.onrender.com/ws?user_id="
    private var webSocketClient : WebSocketClient? = null
    private val okHttpClient = OkHttpClient()

    private val _status = MutableStateFlow("Disconnected")
    val status: StateFlow<String> = _status

    private val _login_status = MutableStateFlow("not logged")
    val login_status: StateFlow<String> = _login_status


    private val _UserMessages = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val UserMessages: StateFlow<Map<Int, List<String>>> = _UserMessages

    private val _userlist = MutableStateFlow<List<Pair<String,Int>>>(emptyList())
    val userlist: StateFlow<List<Pair<String,Int>>> = _userlist


    fun WebSocketInit(my_id: Int?) {
        webSocketClient = WebSocketClient(webSocketUrl + my_id, AppWebSocketListener(::onMessageReceived, ::onStatusChanged))
        connect()
    }

    private fun connect(){
        viewModelScope.launch {
            viewModelScope.launch {
                _status.value = "Connecting..."
                webSocketClient?.connect()
            }
        }
    }

    fun login(context: Context, username: String, password: String){

        Log.i("ChatViewModel_TAG", "Trying to login with username: $username and password: $password")




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
                        _login_status.value = "success"
                        Log.i("ChatViewModel_TAG", "id: ${result?.id}")
                        saveUsernameAndId(context, username, result?.id!!)
                        WebSocketInit(result?.id)

                    }




                }

            }
        })

    }

    fun SignUp(context: Context, username: String, password: String){

        Log.i("ChatViewModel_TAG", "Trying to Sign Up with username: $username and password: $password")




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
                        saveUsernameAndId(context, username, result?.id!!)
                        WebSocketInit(result?.id)

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

    fun LogOut(context: Context){
        saveUsernameAndId(context, "", null)
        webSocketClient?.disconnect()
        _status.value = "Disconnected"
    }

    fun sendMessage(reciever_id: Int, message: String) {
        if (message.isNotBlank()) {
            val jsonConverter = Json
            val data = WebSocketSendingData(reciever_id, message)
            val jsonString = jsonConverter.encodeToString(data)
            webSocketClient?.sendMessage(jsonString)


            val currentMessagesForUser = _UserMessages.value[reciever_id] ?: emptyList()
            val updatedMessagesForUser = currentMessagesForUser + "You: ${message}"
            _UserMessages.value = _UserMessages.value + (reciever_id to updatedMessagesForUser)

        }
    }

    private fun onMessageReceived(message: String) {
        // Update UI state on the main thread
        viewModelScope.launch {
            val json = Json { ignoreUnknownKeys = true }
            val result = message.let { json.decodeFromString<MessageData>(it) }
            val currentMessagesForUser = _UserMessages.value[result.from] ?: emptyList()
            val updatedMessagesForUser = currentMessagesForUser + "${result.username}: ${result.text}"
            _UserMessages.value = _UserMessages.value + (result.from to updatedMessagesForUser)


        }
    }

    private fun onStatusChanged(newStatus: String) {
        viewModelScope.launch {
            _status.value = newStatus
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketClient?.disconnect()
    }




}