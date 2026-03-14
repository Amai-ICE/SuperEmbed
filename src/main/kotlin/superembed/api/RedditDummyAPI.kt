package uk.amaiice.superembed.api

import io.ktor.http.Url
import uk.amaiice.superembed.api.data.IAPIData
import uk.amaiice.superembed.api.data.MissingData

object RedditDummyAPI: AbstractHttpClient() {
    override suspend fun supports(url: Url): Boolean {
        return false
    }

    override suspend fun fetchUrl(url: String): IAPIData {
        return MissingData
    }
}