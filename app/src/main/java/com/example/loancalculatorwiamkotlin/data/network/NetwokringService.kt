package com.example.loancalculatorwiamkotlin.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import com.example.loancalculatorwiamkotlin.data.dto.LoanResponse

class NetworkingService(private val context: Context? = null) {
    val baseURL = "https://jsonplaceholder.typicode.com/posts"

    private val connectivityManager: ConnectivityManager?
        get() = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    fun monitorInternetAvailability(): kotlinx.coroutines.flow.Flow<Boolean> =
        if (context == null) {
            flowOf(true)
        } else {
            callbackFlow {
                var isFirstCheck = true

                val networkCallback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        isFirstCheck = false
                        trySend(true)
                    }

                    override fun onLost(network: Network) {
                        super.onLost(network)
                        isFirstCheck = false
                        trySend(false)
                    }
                }

                // Check current state
                val isConnected = isNetworkConnected()
                isFirstCheck = false
                trySend(isConnected)

                connectivityManager?.registerDefaultNetworkCallback(networkCallback)

                awaitClose {
                    connectivityManager?.unregisterNetworkCallback(networkCallback)
                }
            }
        }

    suspend fun sendRequest(jsonBody: String): LoanResponse {
        return try {
            val url = URL(baseURL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            connection.outputStream.use { output ->
                output.write(jsonBody.toByteArray())
                output.flush()
            }

            if (connection.responseCode !in 200..299) {
                throw IOException("HTTP ${connection.responseCode}")
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }

            json.decodeFromString<LoanResponse>(responseBody)
        } catch (e: Exception) {
            throw IOException("Network request failed", e)
        }
    }

    private fun isNetworkConnected(): Boolean {
        val activeNetwork = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork) ?: return false

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    companion object {
        private var instance: NetworkingService? = null

        fun getInstance(context: Context): NetworkingService {
            return instance ?: synchronized(this) {
                instance ?: NetworkingService(context).also { instance = it }
            }
        }
    }
}

private val json = Json {
    ignoreUnknownKeys = true
}