package uk.amaiice.superembed.api.twitter

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import uk.amaiice.superembed.api.AbstractHttpClient
import uk.amaiice.superembed.api.data.FxTwitterResponse
import uk.amaiice.superembed.api.data.IAPIData

object FxTwitterAPI : AbstractHttpClient() {
    // x.comをapi.fxtwitter.comに変換
    private fun Url.formatUrl(): String {
        return URLBuilder(this).apply {
            protocol = URLProtocol.HTTPS
            host = "api.fxtwitter.com"
        }.buildString()
    }

    override suspend fun supports(url: Url): Boolean {
        val supportedUrls = listOf("x.com", "www.x.com", "twitter.com", "www.twitter.com")
        return supportedUrls.contains(url.host)
    }

    override suspend fun fetchUrl(url: String): IAPIData {
        return client.get(Url(url).formatUrl()) {
            parameter("url", url)
        }.body() as FxTwitterResponse
    }
}