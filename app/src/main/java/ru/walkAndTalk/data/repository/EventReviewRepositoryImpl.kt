package ru.walkAndTalk.data.repository

import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.mapper.toDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.EventReview
import ru.walkAndTalk.domain.repository.EventReviewRepository

class EventReviewRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper,
) : EventReviewRepository {
    override suspend fun fetchReview(userId: String, eventId: String): EventReview? {
        return supabaseWrapper.postgrest[Table.EVENT_REVIEWS]
            .select {
                filter {
                    eq("user_id", userId)
                    eq("event_id", eventId)
                }
            }
            .decodeSingleOrNull<ru.walkAndTalk.data.model.EventReviewDto>()
            ?.toDomain()
    }

    override suspend fun deleteReview(userId: String, eventId: String): Boolean {
        return try {
            supabaseWrapper.postgrest[Table.EVENT_REVIEWS]
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("event_id", eventId)
                    }
                }
            true
        } catch (e: Exception) {
            false
        }
    }

//    override suspend fun submitReview(review: EventReview): Boolean {
//        return try {
//            val existingReview = fetchReview(review.userId, review.eventId)
//            if (existingReview != null) {
//                supabaseWrapper.postgrest[Table.EVENT_REVIEWS]
//                    .update({
//                        set("rating", review.rating)
//                        set("comment", review.comment ?: "")
//                    }) {
//                        filter {
//                            eq("id", existingReview.id!!)
//                        }
//                    }
//            } else {
//                supabaseWrapper.postgrest[Table.EVENT_REVIEWS]
//                    .insert(review.copy(createdAt = null).toDto()) // createdAt set by DB
//            }
//            true
//        } catch (e: Exception) {
//            false
//        }
//    }

    override suspend fun submitReview(review: EventReview): Boolean {
        try {
            val existingReview = fetchReview(review.userId, review.eventId)
            if (existingReview != null) {
                supabaseWrapper.postgrest[Table.EVENT_REVIEWS]
                    .update({
                        set("rating", review.rating)
                        set("comment", review.comment ?: "")
                    }) {
                        filter { eq("id", existingReview.id!!) }
                    }
            } else {
                supabaseWrapper.postgrest[Table.EVENT_REVIEWS]
                    .insert(review.copy(createdAt = null).toDto())
            }
            return true
        } catch (e: Exception) {
            throw java.lang.Exception("Failed to submit review", e)
        }
    }
}
