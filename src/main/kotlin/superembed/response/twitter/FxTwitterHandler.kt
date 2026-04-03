package uk.amaiice.superembed.response.twitter

import dev.kord.common.Color
import dev.kord.common.entity.MessageFlag
import dev.kord.common.entity.MessageFlags
import dev.kord.common.entity.SeparatorSpacingSize
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.rest.builder.component.actionRow
import dev.kord.rest.builder.component.mediaGallery
import dev.kord.rest.builder.component.section
import dev.kord.rest.builder.component.separator
import dev.kord.rest.builder.message.container
import uk.amaiice.superembed.Logger.logger
import uk.amaiice.superembed.api.APIRouter
import uk.amaiice.superembed.api.data.FxTwitterResponse
import uk.amaiice.superembed.api.data.Tweet
import uk.amaiice.superembed.config.TomlData
import uk.amaiice.superembed.extend.DiscordMessageExtender
import uk.amaiice.superembed.extend.DiscordMessageExtender.toDiscordMessage
import uk.amaiice.superembed.handler.IMessageHandler
import uk.amaiice.superembed.util.SiFormatter.toSiFormat

object FxTwitterHandler : IMessageHandler {
    private const val MAX_COMPONENT_TEXT_LENGTH = 4000
    private const val TRUNCATE_SUFFIX = "..."

    enum class EngagementType(val iconID: String) {
        REPLIES("<:message:1470298416843067495>"),
        RETWEETS("<:repeat:1470301607202914304>"),
        LIKES("<:heart:1470301625082974229>"),
        BOOKMARKS("<:bookmark:1470301656297111664>"),
        VIEWS("<:graph:1470301640228601897>")
    }

    fun Int.getEngagement(type: EngagementType): Pair<String, String> {
        return type.iconID to this.toSiFormat()
    }

    fun Tweet.getEngagements(): String {
        val engagements = mutableListOf<Pair<String, String>>()
        engagements.add(this.replies.getEngagement(EngagementType.REPLIES))
        engagements.add(this.retweets.getEngagement(EngagementType.RETWEETS))
        engagements.add(this.likes.getEngagement(EngagementType.LIKES))
        engagements.add(this.bookmarks.getEngagement(EngagementType.BOOKMARKS))
        engagements.add(this.views.getEngagement(EngagementType.VIEWS))

        //エンゲージメントが0のものを除外
        val filteredEngagements = engagements.filter { it.second != "0" }

        return filteredEngagements.joinToString(" ") { "${it.first} ${it.second}" }
    }

    private fun String.toComponentSafeText(fallback: String = "\u200B"): String {
        val normalized = ifBlank { fallback }
        if (normalized.length <= MAX_COMPONENT_TEXT_LENGTH) return normalized

        val keepLength = (MAX_COMPONENT_TEXT_LENGTH - TRUNCATE_SUFFIX.length).coerceAtLeast(1)
        return normalized.take(keepLength) + TRUNCATE_SUFFIX
    }

    // ComponentV2のメッセージをハンドルできないのなんとかしてくれませんかね:rage:
    override suspend fun handle(message: Message) {
        val urlRegex = Regex("""https?://[^\s<>()]+""")
        val urls = urlRegex.findAll(message.content).map { it.value }.toList()
        if (urls.isEmpty()) return

        if (message.content.contains("<${urls.first()}>")) return
        val data = APIRouter.urlFetcher(urls.first())


        if (data is FxTwitterResponse) {
            if (TomlData.get().ignoreFeatures.fxTwitter.contains(message.data.guildId.value?.value?.toString())) {
                logger.info { "Ignored handling Twitter by ${message.author?.id} to URL: ${urls.first()} in guild ${message.data.guildId.value}" }
                return
            }
            runCatching {
                message.channel.createMessage {
                    flags = MessageFlags(MessageFlag.IsComponentsV2)

                    logger.info { "Called Handling Twitter by ${message.author?.id} to URL: ${urls.first()}" }

                    var tweet: Tweet? = data.tweet

                    container {
                        do {
                            val author = tweet!!.author

                            val mediaUrls = tweet.media?.all?.map { it.url }?.filter { it.isNotBlank() }.orEmpty()
                            val hasThumbnail = author.avatarUrl.isNotBlank()
                            val useMediaGallery = mediaUrls.isNotEmpty()
                            accentColor = Color(java.awt.Color.CYAN.rgb)

                            if (hasThumbnail) {
                                section {
                                    val accountLink =
                                        "@${author.screenName}".toDiscordMessage { link(author.url) }
                                    val accountDisplayName = author.name.toDiscordMessage { bold() }
                                    textDisplay(
                                        "$accountLink / $accountDisplayName".toComponentSafeText(),
                                    )
                                    textDisplay(tweet!!.text.toComponentSafeText())
                                    thumbnailAccessory {
                                        url = author.avatarUrl
                                        // Kord 0.18.0 で null を踏むケースを避けるため明示指定
                                        description = "${author.screenName} avatar"
                                    }
                                }
                            } else {
                                textDisplay("@${author.screenName} / ${author.name}".toComponentSafeText())
                                textDisplay(tweet.text.toComponentSafeText())
                            }

                            if (useMediaGallery) {
                                separator {
                                    spacing = SeparatorSpacingSize.Small
                                }

                                mediaGallery {
                                    mediaUrls.forEach { mediaUrl ->
                                        item(mediaUrl)
                                    }
                                }
                            } else if (mediaUrls.isNotEmpty()) {
                                textDisplay(mediaUrls.joinToString("\n").toComponentSafeText())
                            }

                            separator {
                                spacing = SeparatorSpacingSize.Small
                            }
                            val simpleTimestamp = "${tweet.createdTimestamp}".toDiscordMessage(
                                DiscordMessageExtender.style {
                                    timestamp(DiscordMessageExtender.TimestampData.SIMPLE)
                                }
                            )

                            val relativeTimestamp = "${tweet.createdTimestamp}".toDiscordMessage(
                                DiscordMessageExtender.style {
                                    timestamp(DiscordMessageExtender.TimestampData.RELATIVE)
                                }
                            )

                            textDisplay(
                                "${tweet.getEngagements()} | Tweeted at $simpleTimestamp/ Relative: $relativeTimestamp".toComponentSafeText(
                                    "-"
                                ).toDiscordMessage { subtext() }
                            )



                            actionRow {
                                linkButton(tweet!!.url) {
                                    label = "View on Twitter"
                                }
                            }



                            tweet = tweet.quote
                            if (tweet != null) {
                                separator {
                                    spacing = SeparatorSpacingSize.Large
                                }
                            }
                        } while (tweet != null)
                    }

                }
            }.onFailure { e ->
                logger.error(e) { "Failed to create fxTwitter embed message for ${message.id}" }
            }

            runCatching {
                message.edit {
                    flags = MessageFlags(MessageFlag.SuppressEmbeds)
                }
            }.fold(
                onSuccess = {},
                onFailure = { e ->
                    message.channel.createMessage {
                        content = """Embedメッセージを非表示にできませんでした。
                            |MessageContentの権限が付与されているか確認してください。
                        """.trimMargin()
                        logger.error { "Failed to suppress embeds for message ${message.id} in channel ${message.channelId}. Error: ${e.message}" }
                    }
                }
            )
        }
    }
}

