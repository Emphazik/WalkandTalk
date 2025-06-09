package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.domain.model.EventReview

interface EventReviewRepository {
    suspend fun fetchReview(userId: String, eventId: String): EventReview?
    suspend fun fetchUserReviews(userId: String): List<EventReview>
    suspend fun submitReview(review: EventReview): Boolean
    suspend fun deleteReview(userId: String, eventId: String): Boolean
}