package com.example.myshroom

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class BlynkApiService {
    private val client = OkHttpClient()

    suspend fun getPinValue(token: String, pin: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://blynk.cloud/external/api/get?token=$token&$pin"
            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    suspend fun setPinValue(token: String, pin: String, value: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "https://blynk.cloud/external/api/update?token=$token&$pin=$value"
            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}



