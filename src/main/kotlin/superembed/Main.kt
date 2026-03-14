package uk.amaiice.superembed

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import uk.amaiice.superembed.config.TomlData
import uk.amaiice.superembed.handler.MessageHandler
import uk.amaiice.superembed.response.twitter.FxTwitterHandler

suspend fun main() {
    // Kord 0.18.0 の ComponentV2 受信デシリアライズ例外ログを一時的に非表示化
    // その他のエラーも消えるため潜在的なバグになるかも〜。kord君早く直して
    //System.setProperty("dev.kord.core.Kord", "off")
    //↑ 0.18.1で治ったらしいからコメントアウトした

    val kord = Kord(TomlData.get().secrets.token)

    MessageHandler.add(FxTwitterHandler)

    kord.on<MessageCreateEvent> {
        MessageHandler.handle(kord, message)
    }

    kord.login {
        @OptIn(PrivilegedIntent::class)
        intents += Intent.MessageContent
    }
}