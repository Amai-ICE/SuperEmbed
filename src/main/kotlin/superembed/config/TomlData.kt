package uk.amaiice.superembed.config

import com.akuleshov7.ktoml.file.TomlFileReader
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import uk.amaiice.superembed.Logger.logger
import uk.amaiice.superembed.serializer.SnowflakeSerializer

object TomlData {
    const val CONFIG_PATH = "config.toml"

    // synchronized lazy の初期化待ちで詰まるケースを避けるため PUBLICATION を使う。
    private val config: Data by lazy(LazyThreadSafetyMode.PUBLICATION) {
        runCatching {
            TomlFileReader.decodeFromFile(Data.serializer(), CONFIG_PATH)
        }.getOrElse {
            logger.error(it) { "Failed to read config file: $CONFIG_PATH" }
            throw ConfigException(buildString {
                appendLine("wrong setting in $CONFIG_PATH!!")
                appendLine(it.message)
            })
        }
    }

    fun get(): Data = config

    @Serializable
    data class Data(
        val secrets: Secrets,
        val ignoreFeatures: IgnoreFeatures,
    )

    @Serializable
    data class Secrets(
        val token: String,
        @Serializable(with = SnowflakeSerializer::class)
        val devServerID: Snowflake,
        val isDebug: Boolean,
    )

    @Serializable
    data class IgnoreFeatures(
        val fxTwitter: List<String>,
    )

    class ConfigException(message: String) : RuntimeException(message)
}
