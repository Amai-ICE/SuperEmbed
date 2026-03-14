package uk.amaiice.superembed.api

import io.ktor.http.*
import kotlinx.coroutines.delay
import uk.amaiice.superembed.api.data.IAPIData
import uk.amaiice.superembed.api.data.MissingData
import uk.amaiice.superembed.api.twitter.FxTwitterAPI

object APIRouter {
    private val handlers = listOf(RedditDummyAPI, FxTwitterAPI)

    private suspend fun Url.route(): RouteResult {
        val host = this.host
        val handler = handlers.firstOrNull { it.supports(this) }
        return if (handler != null) RouteResult.Supported(handler) else RouteResult.Unsupported(host)
    }

    suspend fun urlFetcher(url: String): IAPIData {
        when (val result = Url(url).route()) {
            is RouteResult.Supported -> {
                var fetchData: IAPIData = MissingData
                for (retry in 0..10) {
                    fetchData = result.handler.fetchUrl(url)
                    if (fetchData.statusCode == 200 || fetchData.statusCode == 404)
                        break

                    println("failed fetch url. retry count: $retry")
                    delay(500)
                }

                return fetchData
            }

            is RouteResult.Unsupported -> return MissingData
        }
    }
}