package uk.amaiice.superembed.api

import io.ktor.client.*
import io.ktor.http.*
import uk.amaiice.superembed.api.data.IAPIData

interface IHttpClient {
    val client: HttpClient
    suspend fun supports(url: Url): Boolean
    suspend fun fetchUrl(url: String): IAPIData
}