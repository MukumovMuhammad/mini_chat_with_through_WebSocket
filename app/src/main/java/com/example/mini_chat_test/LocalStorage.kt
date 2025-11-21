package com.example.mini_chat_test

import android.content.Context

fun saveToSharedPreferences(context: Context, key: String, value: String) {
    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString(key, value)
        apply()
    }
}

fun saveUsernameAndId(context: Context, username: String, id: Int?) {
    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString("id", id.toString())
        putString("username", username)
        apply()
    }
}


fun getSavedId(context: Context): String? {
    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return sharedPref.getString("id", null)
}
fun getSavedUsername(context: Context): String? {
    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return sharedPref.getString("username", null)
}
