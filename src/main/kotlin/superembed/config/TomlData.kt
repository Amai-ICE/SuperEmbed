package uk.amaiice.superembed.config

import com.akuleshov7.ktoml.file.TomlFileReader
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import uk.amaiice.superembed.serializer.SnowflakeSerializer
import java.io.File
import kotlin.system.exitProcess

object TomlData {
    const val CONFIG_PATH = "config.toml"

    private val config: Data by lazy {
        runCatching {
            TomlFileReader.decodeFromFile(Data.serializer(), CONFIG_PATH)
        }.fold(
            {return@fold it},
            {
                println("failed to read config file.")
                throw ConfigException(buildString {
                    appendLine("wrong setting in $CONFIG_PATH!!")
                    appendLine(it.message)
                })
            }
        )
    }

    fun get(): Data = config

    @Serializable
    data class Data(val secrets: Secrets)

    @Serializable
    data class Secrets(val token: String,@Serializable(with = SnowflakeSerializer::class) val devServerID: Snowflake)

    class ConfigException(message: String): RuntimeException(message)
}
