package uk.amaiice.superembed.handler

import dev.kord.core.entity.Message
import uk.amaiice.superembed.Logger.logger
import uk.amaiice.superembed.config.TomlData
import uk.amaiice.superembed.isDebug

object MessageHandler : AbstractHandler() {
    suspend fun handle(message: Message) {
        if (message.author == null) return
        if (message.author!!.isBot) return
        if (isDebug) {
            logger.debug { "Debug mode is enabled! return MessageHandler!" }
            val guildid = message.data.guildId.value ?: return
            if (guildid.value != TomlData.get().secrets.devServerID.value) return
        }

        handlers.forEach { handler ->
            if (handler is IMessageHandler) {
                logger.debug { "Handler ${handler::class.simpleName} is handling message ${message.id.value}" }
                handler.handle(message)
            }
        }
    }
}