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
import uk.amaiice.superembed.extend.DiscordMessageExtender
import uk.amaiice.superembed.extend.DiscordMessageExtender.toDiscordMessage
import uk.amaiice.superembed.handler.IMessageHandler
import uk.amaiice.superembed.util.SiFormatter.toSiFormat

object FxTwitterHandler : IMessageHandler {
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

    // ComponentV2のメッセージをハンドルできないのなんとかしてくれませんかね:rage:
    override suspend fun handle(message: Message) {
        val urlRegex = Regex("""https?://[^\s<>()]+""")

        val urls = urlRegex.findAll(message.content).map { it.value }.toList()
        if (urls.isEmpty()) return

        if (message.content.contains("<${urls.first()}>")) return
        val data = APIRouter.urlFetcher(urls.first())


        if (data is FxTwitterResponse) {
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
                        val useLinkButton = tweet.url.isNotBlank()
                        accentColor = Color(java.awt.Color.CYAN.rgb)

                        if (hasThumbnail) {
                            section {
                                val accountLink =
                                    "@${author.screenName}".toDiscordMessage { link(author.url) }
                                val accountDisplayName = author.name.toDiscordMessage { bold() }
                                textDisplay(
                                    "$accountLink / $accountDisplayName",
                                )
                                textDisplay(tweet!!.text)
                                thumbnailAccessory {
                                    url = author.avatarUrl
                                    // Kord 0.18.0 で null を踏むケースを避けるため明示指定
                                    description = "${author.screenName} avatar"
                                }
                            }
                        } else {
                            textDisplay("@${author.screenName} / ${author.name}")
                            textDisplay(tweet.text)
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
                            textDisplay(mediaUrls.joinToString("\n"))
                        }

                        separator {
                            spacing = SeparatorSpacingSize.Small
                        }

                        textDisplay(
                            tweet.getEngagements()
                                .toDiscordMessage { subtext() }
                        )

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
                            "Tweeted at $simpleTimestamp/ Relative: $relativeTimestamp".toDiscordMessage { subtext() }
                        )

                        if (useLinkButton) {
                            actionRow {
                                linkButton(tweet!!.url) {
                                    label = "View on Twitter"
                                }
                            }
                        } else if (tweet.url.isNotBlank()) {
                            separator {
                                spacing = SeparatorSpacingSize.Small
                            }
                            textDisplay(tweet.url)
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

