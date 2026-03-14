package uk.amaiice.superembed.handler

import dev.kord.core.Kord
import dev.kord.core.entity.Message
import uk.amaiice.superembed.config.TomlData

object MessageHandler {
    private val handler = mutableListOf<IMessageHandler>()

    fun add(handler: IMessageHandler) {
        this.handler.add(handler)
    }

    suspend fun handle(kord: Kord, message: Message) {
        if (message.author == null) return
        if (message.author!!.isBot) return
        val guildid = message.data.guildId.value ?: return
        if (guildid.value != TomlData.get().secrets.devServerID.value) return

        handler.forEach { handler ->
            handler.handle(message)
        }
    }
}