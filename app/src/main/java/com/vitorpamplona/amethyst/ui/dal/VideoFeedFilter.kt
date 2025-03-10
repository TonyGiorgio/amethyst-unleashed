package com.vitorpamplona.amethyst.ui.dal

import com.vitorpamplona.amethyst.model.Account
import com.vitorpamplona.amethyst.model.GLOBAL_FOLLOWS
import com.vitorpamplona.amethyst.model.LocalCache
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.model.TimeUtils
import com.vitorpamplona.amethyst.service.model.*

class VideoFeedFilter(val account: Account) : AdditiveFeedFilter<Note>() {
    override fun feedKey(): String {
        return account.userProfile().pubkeyHex + "-" + account.defaultStoriesFollowList
    }

    override fun showHiddenKey(): Boolean {
        return account.defaultStoriesFollowList == PeopleListEvent.blockList
    }

    override fun feed(): List<Note> {
        val notes = innerApplyFilter(LocalCache.notes.values)

        return sort(notes)
    }

    override fun applyFilter(collection: Set<Note>): Set<Note> {
        return innerApplyFilter(collection)
    }

    private fun innerApplyFilter(collection: Collection<Note>): Set<Note> {
        val now = TimeUtils.now()
        val isGlobal = account.defaultStoriesFollowList == GLOBAL_FOLLOWS
        val isHiddenList = account.defaultStoriesFollowList == PeopleListEvent.blockList

        val followingKeySet = account.selectedUsersFollowList(account.defaultStoriesFollowList) ?: emptySet()
        val followingTagSet = account.selectedTagsFollowList(account.defaultStoriesFollowList) ?: emptySet()
        val followingGeohashSet = account.selectedGeohashesFollowList(account.defaultStoriesFollowList) ?: emptySet()

        return collection
            .asSequence()
            .filter { it.event is FileHeaderEvent || it.event is FileStorageHeaderEvent }
            .filter { isGlobal || it.author?.pubkeyHex in followingKeySet || (it.event?.isTaggedHashes(followingTagSet) ?: false) || (it.event?.isTaggedGeoHashes(followingGeohashSet) ?: false) }
            .filter { isHiddenList || account.isAcceptable(it) }
            .filter { it.createdAt()!! <= now }
            .toSet()
    }

    override fun sort(collection: Set<Note>): List<Note> {
        return collection.sortedWith(compareBy({ it.createdAt() }, { it.idHex })).reversed()
    }
}
