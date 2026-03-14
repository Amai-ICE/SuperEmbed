package uk.amaiice.superembed.extend

object DiscordMessageExtender {
    enum class TimestampData {
        RELATIVE, SIMPLE, FORMATTED, FORMATTED_WITH_WEEKLY, EMPTY,
    }

    fun String.toDiscordMessage(discordMessageData: DiscordMessageData): String {
        var result = this

        if (discordMessageData.timestampData != TimestampData.EMPTY) {
            result = when (discordMessageData.timestampData) {
                //11時間前
                TimestampData.RELATIVE -> {
                    "<t:$result:R>"
                }
                //2026/03/12 0:11:27
                TimestampData.SIMPLE -> {
                    "<t:$result:S>"
                }
                //2026年3月12日 0:11
                TimestampData.FORMATTED -> {
                    "<t:$result:f>"
                }
                //2026年3月12日木曜日 0:11
                TimestampData.FORMATTED_WITH_WEEKLY -> {
                    "<t:$result:F>"
                }
            }
        }
        if (discordMessageData.isBold) {
            result = result.toBold()
        }
        if (discordMessageData.isSubtext) {
            result = result.toSubtext()
        }
        if (discordMessageData.hasLink.isNotEmpty()) {
            result = result.toLinkMessage(discordMessageData.hasLink)
        }

        return result
    }

    private fun String.toLinkMessage(url: String): String {
        return "[$this]($url)"
    }

    private fun String.toBold(): String {
        return "**$this**"
    }

    private fun String.toSubtext(): String {
        return "-# $this"
    }
}