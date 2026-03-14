package uk.amaiice.superembed.handler

import dev.kord.core.entity.Message

interface IMessageHandler {
    suspend fun handle(message: Message)
}