package uk.amaiice.superembed.api

import io.ktor.http.*
import kotlinx.coroutines.delay
import uk.amaiice.superembed.Logger.logger
import uk.amaiice.superembed.api.data.IAPIData
import uk.amaiice.superembed.api.data.MissingData
import uk.amaiice.superembed.api.twitter.FxTwitterAPI

object APIRouter {
    private const val MAX_RETRY_COUNT = 10
    private const val RETRY_DELAY_MILLIS = 500L
    private val TERMINAL_STATUS_CODES = setOf(200, 404)

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
                for (retry in 0..MAX_RETRY_COUNT) {
                    fetchData = result.handler.fetchUrl(url)
                    if (fetchData.statusCode in TERMINAL_STATUS_CODES)
                        break

                    logger.warn { "Failed fetch url (status=${fetchData.statusCode}). retry count: $retry" }
                    delay(RETRY_DELAY_MILLIS)
                }

                return fetchData
            }

            is RouteResult.Unsupported -> return MissingData
        }
    }
}