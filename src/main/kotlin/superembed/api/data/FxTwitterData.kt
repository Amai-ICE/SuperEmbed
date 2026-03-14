package uk.amaiice.superembed.api.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class FxTwitterResponse(
    @SerialName("code")
    override val statusCode: Int,
    override val message: String,
    val tweet: Tweet
) : IAPIData

@Serializable
data class Tweet(
    val url: String,
    val id: String,
    val text: String,
    val author: Author,
    // media存在しなかったりするため、nullable
    val media: Media? = null,
    @SerialName("created_timestamp")
    val createdTimestamp: Int,
    val replies: Int,
    val retweets: Int,
    val likes: Int,
    val bookmarks: Int,
    val views: Int,
    val quote: Tweet? = null
)

@Serializable
data class Author(
    val id: String,
    val name: String,
    @SerialName("screen_name")
    val screenName: String,
    @SerialName("avatar_url")
    val avatarUrl: String,
    val url: String
)

@Serializable
data class Media(
    val all: List<MediaData>,
)

@Serializable
data class MediaData(
    val url: String
)
