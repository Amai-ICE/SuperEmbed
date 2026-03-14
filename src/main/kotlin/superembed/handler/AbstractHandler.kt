package uk.amaiice.superembed.handler

import uk.amaiice.superembed.Logger.logger

abstract class AbstractHandler {
    val handlers = mutableListOf<IHandler>()

    fun add(handler: IHandler) {
        logger.info { "Adding handler $handler" }
        handlers.add(handler)
    }
}