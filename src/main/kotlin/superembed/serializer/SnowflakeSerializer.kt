package uk.amaiice.superembed.serializer

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// TOMLでシリアライズするためのシリアライザ.
object SnowflakeSerializer : KSerializer<Snowflake> {
    //内部実装的にはULongだが、シリアライザ側ではLongまでしか扱えないため、Stringで実装.
    override val descriptor: SerialDescriptor
        = PrimitiveSerialDescriptor("Snowflake", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Snowflake {
        return Snowflake(decoder.decodeString().toULong())
    }

    override fun serialize(encoder: Encoder, value: Snowflake) {
        encoder.encodeString(value.value.toString())
    }
}