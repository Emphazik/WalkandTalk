package ru.walkAndTalk.data.usecase

import ru.walkAndTalk.domain.model.FeedItem
import ru.walkAndTalk.domain.model.FeedItemType
import ru.walkAndTalk.domain.repository.FeedRepository

class FetchFeedItemsUseCase(
    private val feedRepository: FeedRepository
) {
    suspend operator fun invoke(city: String? = null, type: FeedItemType? = null): List<FeedItem> {
        return feedRepository.fetchFeedItems(city, type)
    }
}