package uk.amaiice.superembed.extend

object DiscordMessageExtender {
    enum class TimestampData {
        RELATIVE, SIMPLE, FORMATTED, FORMATTED_WITH_WEEKLY, EMPTY,
    }

    data class Style(
        val timestamp: TimestampData = TimestampData.EMPTY,
        val heading: Boolean = false,
        val bold: Boolean = false,
        val italic: Boolean = false,
        val subtext: Boolean = false,
        val link: String = "",
    )

    class StyleBuilder {
        private var timestamp: TimestampData = TimestampData.EMPTY
        private var heading: Boolean = false
        private var bold: Boolean = false
        private var italic: Boolean = false
        private var subtext: Boolean = false
        private var link: String = ""

        fun timestamp(value: TimestampData) {
            timestamp = value
        }

        fun heading() {
            heading = true
        }

        fun bold() {
            bold = true
        }

        fun italic() {
            italic = true
        }

        fun subtext() {
            subtext = true
        }

        fun link(url: String) {
            link = url
        }

        internal fun build(): Style {
            return Style(
                timestamp = timestamp,
                heading = heading,
                bold = bold,
                italic = italic,
                subtext = subtext,
                link = link,
            )
        }
    }

    fun style(build: StyleBuilder.() -> Unit): Style {
        val builder = StyleBuilder()
        builder.apply(build)
        return builder.build()
    }

    fun String.toDiscordMessage(build: StyleBuilder.() -> Unit): String {
        return this.toDiscordMessage(style(build))
    }

    fun String.toDiscordMessage(style: Style): String {
        var result = this

        if (style.timestamp != TimestampData.EMPTY) {
            result = when (style.timestamp) {
                TimestampData.RELATIVE -> "<t:$result:R>"
                TimestampData.SIMPLE -> "<t:$result:S>"
                TimestampData.FORMATTED -> "<t:$result:f>"
                TimestampData.FORMATTED_WITH_WEEKLY -> "<t:$result:F>"
                TimestampData.EMPTY -> result
            }
        }

        if (style.heading) {
            result = result.toH1Text()
        }
        if (style.bold) {
            result = result.toBold()
        }
        if (style.italic) {
            result = result.toItalic()
        }
        if (style.subtext) {
            result = result.toSubtext()
        }
        if (style.link.isNotEmpty()) {
            result = result.toLinkMessage(style.link)
        }

        return result
    }

    private fun String.toH1Text(): String {
        return "# $this"
    }

    private fun String.toLinkMessage(url: String): String {
        return "[$this]($url)"
    }

    private fun String.toBold(): String {
        return "**$this**"
    }

    private fun String.toItalic(): String {
        return "*$this*"
    }

    private fun String.toSubtext(): String {
        return "-# $this"
    }
}