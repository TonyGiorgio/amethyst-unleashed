package com.vitorpamplona.amethyst

object OptOutFromFilters {
    var warnAboutPostsWithReports: Boolean = false
    var filterSpamFromStrangers: Boolean = false

    fun start(warnAboutReports: Boolean, filterSpam: Boolean) {
        warnAboutPostsWithReports = warnAboutReports
        filterSpamFromStrangers = filterSpam
    }
}
