package com.example.mini_chat_test.websocket

import AppWebSocketListener
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mini_chat_test.UserDataResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class ChatViewModel: ViewModel() {

    private val serverUrl = "http://192.168.123.40:4000/"
    private val webSocketUrl = "ws://192.168.123.40:4000/ws?user_id="
    private var webSocketClient : WebSocketClient? = null
    private val okHttpClient = OkHttpClient()

    private val _status = MutableStateFlow("Disconnected")
    val status: StateFlow<String> = _status

    private val _login_status = MutableStateFlow("not logged")
    val login_status: StateFlow<String> = _login_status


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

    fun login(username: String, password: String){

        Log.i("ChatViewModel_TAG", "Trying to login with username: $username and password: $password")



//        val httpUrl  = HttpUrl.Builder()
//            .scheme("http")
//            .host("192.168.0.121")
//            .port(8080)
//            .addPathSegment("login")
//            .addQueryParameter("username", username)
//            .addQueryParameter("client_id", client_id.toString())
//            .build()



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
                val body = response.body?.string()
                println("Response: $body")
            }
        })
    }
    fun sendMessage(message: String) {
        if (message.isNotBlank()) {
            webSocketClient?.sendMessage(message)
        }
    }

    private fun onMessageReceived(message: String) {
        // Update UI state on the main thread
        viewModelScope.launch {
//            _messages.value = _messages.value + message
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