package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.EventReviewDto
import ru.walkAndTalk.domain.model.EventReview

fun EventReviewDto.toDomain(): EventReview {
    return EventReview(
        id = id,
        eventId = eventId,
        userId = userId,
        rating = rating,
        comment = comment,
        createdAt = createdAt
    )
}

fun EventReview.toDto(): EventReviewDto {
    return EventReviewDto(
        id = id,
        eventId = eventId,
        userId = userId,
        rating = rating,
        comment = comment,
        createdAt = createdAt
    )
}