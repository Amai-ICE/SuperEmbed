package uk.amaiice.superembed.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import uk.amaiice.superembed.api.data.IAPIData

abstract class AbstractHttpClient: IHttpClient {
    override val client: HttpClient
        = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
    }
}