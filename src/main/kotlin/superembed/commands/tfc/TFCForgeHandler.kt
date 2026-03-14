package uk.amaiice.superembed.commands.tfc

import dev.kord.common.entity.MessageFlag
import dev.kord.common.entity.MessageFlags
import dev.kord.common.entity.SeparatorSpacingSize
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.component.separator
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.container
import uk.amaiice.superembed.Logger.logger
import uk.amaiice.superembed.commands.tfc.TFCForgeCalculator.calculate
import uk.amaiice.superembed.commands.tfc.TFCForgeCalculator.plusHammers
import uk.amaiice.superembed.config.TomlData
import uk.amaiice.superembed.extend.DiscordMessageData
import uk.amaiice.superembed.extend.DiscordMessageExtender.toDiscordMessage
import uk.amaiice.superembed.handler.CommandHandler
import uk.amaiice.superembed.handler.ICommandHandler

object TFCForgeHandler : ICommandHandler {
    private const val SECTION_ACCESSORY_ICON_URL = "https://cdn.discordapp.com/embed/avatars/0.png"

    override val commandData: CommandHandler.GuildCommandData = CommandHandler.GuildCommandData(
        guildID = TomlData.get().secrets.devServerID,
        name = "forge",
        description = "TFCの鍛冶レシピを計算します。",
        guildChatInputCommandInteraction = {
            integer("hammer", "赤の矢印が指す値") {
                required = true
            }
            string("sequence", "最後に叩く値を入力(例: -3, 7, -15)") {
                required = true
            }
        }
    )

    private val emojiList = mapOf<Int, String>(
        -3 to "<:3_:1380859818650370198>",
        -6 to "<:6_:1380859832353161256>",
        -9 to "<:9_:1380859842784133131>",
        -15 to "<:15:1380859851004973158>",

        2 to "<:2_:1380859857841819698>",
        7 to "<:7_:1380859878599561307>",
        13 to "<:13:1380859887340486656>",
        16 to "<:16:1380859896165171272>"
    )

    private fun List<Int>.emojiMapper(): String {
        return this.joinToString("") { emojiList[it].toString() }
    }

    private fun List<Int>.hammerCountParser(): List<Int> {
        val result = mutableListOf<Int>()
        this.forEachIndexed { index, plusHammer ->
            for (i in 0..<plusHammer) {
                result.add(plusHammers[index])
            }
        }
        return result
    }

    override suspend fun handle(interaction: GuildChatInputCommandInteraction) {
        val command = interaction.command
        val hammer = command.integers["hammer"] ?: return
        val sequence = command.strings["sequence"] ?: return
        val sequenceList = sequence.split(",").map { it.trim().toIntOrNull() ?: return }

        logger.info { "Called TFC Forge calculator from ${interaction.user.id} (${hammer}): $sequenceList" }

        when (hammer.toInt()) {
            in 200..Int.MAX_VALUE -> {
                interaction.respondPublic {
                    content = "目標値は200未満でなければなりません。"
                }
                logger.error { "User(${interaction.user.id}) inputted target value is too large." }
                return
            }

            in Int.MIN_VALUE..-20 -> {
                interaction.respondPublic {
                    content = "目標値は-20以上でなければなりません。"
                }
                logger.error { "User(${interaction.user.id}) inputted target value is too small" }
                return
            }
        }

        val data = TFCForgeData(hammer.toInt(), sequenceList)

        val (errorMessage, result) = calculate(data)
        if (errorMessage.isNotEmpty()) {
            interaction.respondPublic {
                content = errorMessage
                logger.error { "User(${interaction.user.id}) occurred $errorMessage" }
            }
        } else {
            val resultList = result.hammerCountParser() + sequenceList
            interaction.respondPublic {
                flags = MessageFlags(MessageFlag.IsComponentsV2)
                container {

                    textDisplay("TFC鍛冶計算")
                    textDisplay("目標値: $hammer, 仕上げ作業: ${sequenceList.emojiMapper()}")

                    separator { spacing = SeparatorSpacingSize.Small }

                    textDisplay("結果")
                    textDisplay(resultList.emojiMapper().toDiscordMessage(DiscordMessageData(ish1Text = true)))
                }
            }
        }
    }

    override suspend fun register(kord: Kord) {
        commandData.let { CommandHandler.registerGlobalCommand(kord, it) }
    }
}