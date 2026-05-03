package com.example.savestate.data.network

import com.example.savestate.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// http client configured to contant RAWG endpoints
val rawgHttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    defaultRequest {
        url {
            protocol = URLProtocol.HTTPS
            host = "api.rawg.io"
            parameters.append("key", BuildConfig.RAWG_API_KEY)
        }
    }
}