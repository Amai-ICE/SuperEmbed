package uk.amaiice.superembed.api

sealed interface RouteResult {
    data class Supported(val handler: IHttpClient): RouteResult
    data class Unsupported(val host: String): RouteResult
}