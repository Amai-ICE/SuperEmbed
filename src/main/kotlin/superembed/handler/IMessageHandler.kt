package uk.amaiice.superembed.handler

import dev.kord.core.entity.Message

interface IMessageHandler : IHandler {
    suspend fun handle(message: Message)
}