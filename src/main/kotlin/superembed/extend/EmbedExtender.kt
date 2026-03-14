package uk.amaiice.superembed.extend

import dev.kord.rest.builder.message.EmbedBuilder

fun EmbedBuilder.separator() {
    field {
        name = EmbedBuilder.ZERO_WIDTH_SPACE
        value = EmbedBuilder.ZERO_WIDTH_SPACE
        inline = false
    }
}