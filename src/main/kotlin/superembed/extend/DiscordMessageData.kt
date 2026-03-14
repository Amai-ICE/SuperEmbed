package uk.amaiice.superembed.extend

data class DiscordMessageData(
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isSubtext: Boolean = false,
    val hasLink: String = "",
    val timestampData: DiscordMessageExtender.TimestampData = DiscordMessageExtender.TimestampData.EMPTY,
)