package uk.amaiice.superembed.handler

import dev.kord.common.entity.Snowflake
import uk.amaiice.superembed.config.TomlData
import uk.amaiice.superembed.isDebug

object HandlerExecutionPolicy {
    fun shouldProcessGuildEvent(guildId: Snowflake?): Boolean {
        if (!isDebug) return true
        val devGuildId = TomlData.get().secrets.devServerID
        return guildId == devGuildId
    }
}

