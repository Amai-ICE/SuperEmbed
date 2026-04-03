package uk.amaiice.superembed

import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import uk.amaiice.superembed.Logger.logger
import uk.amaiice.superembed.commands.tfc.TFCForgeHandler
import uk.amaiice.superembed.config.TomlData
import uk.amaiice.superembed.handler.CommandHandler
import uk.amaiice.superembed.handler.MessageHandler
import uk.amaiice.superembed.response.twitter.FxTwitterHandler

val isDebug: Boolean
    get() = TomlData.get().secrets.isDebug

suspend fun main() {
    val config = TomlData.get()

    if (config.secrets.isDebug) {
        // kotlin-logging (slf4j-simple) の DEBUG 出力を有効化
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug")

        logger.warn { "Debug mode is enabled" }
        logger.debug { "THIS IS DEBUG MESSAGE." }
    }
    // Kord 0.18.0 の ComponentV2 受信デシリアライズ例外ログを一時的に非表示化
    // その他のエラーも消えるため潜在的なバグになるかも〜。kord君早く直して
    //System.setProperty("dev.kord.core.Kord", "off")
    //↑ 0.18.1で治ったらしいからコメントアウトした

    val kord = Kord(config.secrets.token)

    logger.info { "Starting SuperEmbed..." }

    MessageHandler.add(FxTwitterHandler)

    kord.on<MessageCreateEvent> {
        MessageHandler.handle(message)
    }

    CommandHandler.add(TFCForgeHandler)

    CommandHandler.registerAllCommand(kord)
    kord.on<GuildChatInputCommandInteractionCreateEvent> {
        CommandHandler.guildCommandHandler(interaction)
    }

    logger.info { "Started SuperEmbed..." }
    kord.login {
        @OptIn(PrivilegedIntent::class)
        intents += Intent.MessageContent
    }
}