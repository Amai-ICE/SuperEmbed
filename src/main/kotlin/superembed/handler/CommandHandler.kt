package uk.amaiice.superembed.handler

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import uk.amaiice.superembed.Logger.logger
import uk.amaiice.superembed.config.TomlData
import uk.amaiice.superembed.isDebug

object CommandHandler : AbstractHandler() {
    data class GuildCommandData(
        val guildID: Snowflake,
        val name: String,
        val description: String,
        val guildChatInputCommandInteraction: ChatInputCreateBuilder.() -> Unit
    )

    suspend fun registerAllCommand(kord: Kord) {
        handlers.forEach { handler ->
            if (handler is ICommandHandler) {
                handler.register(kord)
            }
        }
    }

    suspend fun registerGlobalCommand(kord: Kord, data: GuildCommandData) {
        kord.createGlobalChatInputCommand(data.name, data.description) {
            data.guildChatInputCommandInteraction.invoke(this)
        }
    }

    suspend fun guildCommandHandler(interaction: GuildChatInputCommandInteraction) {
        if (interaction.user.isBot) return
        if (isDebug) {
            logger.debug { "Debug mode is enabled! return: ${interaction.command.rootName}" }
            val guildid = interaction.data.guildId.value ?: return
            if (guildid.value != TomlData.get().secrets.devServerID.value) return
        }

        handlers.forEach { handler ->
            if (handler is ICommandHandler) {
                if (handler.commandData.name == interaction.command.rootName)
                    logger.debug { "Handler ${handler::class.simpleName} is handling command ${interaction.command.rootName}" }
                handler.handle(interaction)
            }
        }
    }
}