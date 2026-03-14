package uk.amaiice.superembed.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import uk.amaiice.superembed.api.data.IAPIData

interface IHttpClient {
    val client: HttpClient
    suspend fun supports(url: Url): Boolean
    suspend fun fetchUrl(url: String): IAPIData
}