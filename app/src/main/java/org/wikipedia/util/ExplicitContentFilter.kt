package org.wikipedia.util

import org.wikipedia.dataclient.page.PageSummary
import org.wikipedia.feed.featured.FeaturedArticleCard
import org.wikipedia.feed.image.FeaturedImageCard
import org.wikipedia.feed.model.BasedOnInterestCard
import org.wikipedia.feed.model.BecauseYouReadCard
import org.wikipedia.feed.model.Card
import org.wikipedia.feed.model.DiscoverCard
import org.wikipedia.feed.model.PlacesOfInterestCard
import org.wikipedia.feed.model.RandomCard
import org.wikipedia.feed.news.NewsCard
import org.wikipedia.feed.topread.TopReadCard

object ExplicitContentFilter {

    private val EXPLICIT_KEYWORDS = setOf(
        // Tamil explicit / adult keywords
        "பாலுறவு", "உடலுறவு", "ஆண்குறி", "பெண்குறி", "குதவழி", "சுயஇன்பம்",
        "காமம்", "ஆபாசம்", "பாலியல்", "இன்புறுதல்", "ஆசனவாய்", "புணர்ச்சி",
        "கற்பழிப்பு", "விபச்சாரம்", "காமசூத்ரா", "காமசூத்திரம்", "போர்னோகிராபி",
        // English explicit / adult keywords
        "anal sex", "oral sex", "sexual intercourse", "pornography", "pornographic",
        "erotic", "erotism", "hentai", "clitoris", "ejaculation", "copulation",
        "fellatio", "cunnilingus", "masturbation", "orgasm", "penis", "vagina",
        "vulva", "genitalia", "fetish", "nude art", "nudity", "porn", "xxx"
    )

    fun isExplicitText(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        val lowerText = text.lowercase()
        return EXPLICIT_KEYWORDS.any { lowerText.contains(it) }
    }

    fun isExplicitSummary(summary: PageSummary?): Boolean {
        if (summary == null) return false
        return isExplicitText(summary.displayTitle) ||
                isExplicitText(summary.apiTitle) ||
                isExplicitText(summary.description) ||
                isExplicitText(summary.extract)
    }

    fun isExplicitCard(card: Card): Boolean {
        return when (card) {
            is FeaturedArticleCard -> isExplicitSummary(card.page)
            is RandomCard -> isExplicitText(card.title.displayText) || isExplicitText(card.title.prefixedText)
            is DiscoverCard -> isExplicitText(card.title.displayText) || isExplicitText(card.title.prefixedText)
            is BecauseYouReadCard -> isExplicitText(card.title.displayText) || isExplicitText(card.sourceDisplayTitle)
            is BasedOnInterestCard -> isExplicitText(card.title.displayText)
            is PlacesOfInterestCard -> isExplicitText(card.title.displayText)
            is FeaturedImageCard -> isExplicitText(card.featuredImage.title) || isExplicitText(card.featuredImage.description?.text)
            is NewsCard -> card.news.any { isExplicitText(it.story) }
            is TopReadCard -> card.articles.articles.any { isExplicitSummary(it) }
            else -> isExplicitText(card.title())
        }
    }
}
