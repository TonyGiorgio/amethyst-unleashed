package com.vitorpamplona.amethyst.service.model

import androidx.compose.runtime.Immutable
import com.vitorpamplona.amethyst.model.HexKey
import com.vitorpamplona.amethyst.model.TimeUtils
import com.vitorpamplona.amethyst.model.toHexKey
import com.vitorpamplona.amethyst.service.CryptoUtils

@Immutable
class ChannelMessageEvent(
    id: HexKey,
    pubKey: HexKey,
    createdAt: Long,
    tags: List<List<String>>,
    content: String,
    sig: HexKey
) : BaseTextNoteEvent(id, pubKey, createdAt, kind, tags, content, sig), IsInPublicChatChannel {

    override fun channel() = tags.firstOrNull {
        it.size > 3 && it[0] == "e" && it[3] == "root"
    }?.get(1) ?: tags.firstOrNull {
        it.size > 1 && it[0] == "e"
    }?.get(1)

    override fun replyTos() = tags.filter { it.firstOrNull() == "e" && it.getOrNull(1) != channel() }.mapNotNull { it.getOrNull(1) }

    companion object {
        const val kind = 42

        fun create(
            message: String,
            channel: String,
            replyTos: List<String>? = null,
            mentions: List<String>? = null,
            zapReceiver: String?,
            privateKey: ByteArray,
            createdAt: Long = TimeUtils.now(),
            markAsSensitive: Boolean,
            zapRaiserAmount: Long?,
            geohash: String? = null
        ): ChannelMessageEvent {
            val content = message
            val pubKey = CryptoUtils.pubkeyCreate(privateKey).toHexKey()
            val tags = mutableListOf(
                listOf("e", channel, "", "root")
            )
            replyTos?.forEach {
                tags.add(listOf("e", it))
            }
            mentions?.forEach {
                tags.add(listOf("p", it))
            }
            zapReceiver?.let {
                tags.add(listOf("zap", it))
            }
            if (markAsSensitive) {
                tags.add(listOf("content-warning", ""))
            }
            zapRaiserAmount?.let {
                tags.add(listOf("zapraiser", "$it"))
            }
            geohash?.let {
                tags.add(listOf("g", it))
            }

            val id = generateId(pubKey, createdAt, kind, tags, content)
            val sig = CryptoUtils.sign(id, privateKey)
            return ChannelMessageEvent(id.toHexKey(), pubKey, createdAt, tags, content, sig.toHexKey())
        }
    }
}

interface IsInPublicChatChannel {
    open fun channel(): String?
}
