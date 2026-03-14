package uk.amaiice.superembed.handler

import dev.kord.core.Kord
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction

interface ICommandHandler : IHandler {
    val commandData: CommandHandler.GuildCommandData
    suspend fun handle(interaction: GuildChatInputCommandInteraction)
    suspend fun register(kord: Kord)
}