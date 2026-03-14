package uk.amaiice.superembed.handler

import dev.kord.core.entity.Message
import uk.amaiice.superembed.Logger.logger

object MessageHandler : AbstractHandler() {
    suspend fun handle(message: Message) {
        val author = message.author ?: return
        if (author.isBot) return

        if (!HandlerExecutionPolicy.shouldProcessGuildEvent(message.data.guildId.value)) {
            logger.debug { "Skip message ${message.id.value}: debug mode guild filter" }
            return
        }

        handlers.forEach { handler ->
            if (handler is IMessageHandler) {
                logger.debug { "Handler ${handler::class.simpleName} is handling message ${message.id.value}" }
                handler.handle(message)
            }
        }
    }
}