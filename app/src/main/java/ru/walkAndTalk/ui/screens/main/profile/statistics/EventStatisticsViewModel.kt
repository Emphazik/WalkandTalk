package ru.walkAndTalk.ui.screens.main.profile.statistics

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.walkAndTalk.domain.model.EventReview
import ru.walkAndTalk.domain.repository.EventReviewRepository
import ru.walkAndTalk.domain.repository.EventsRepository
import ru.walkAndTalk.ui.orbit.ContainerViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EventStatisticsViewModel(
    private val userId: String,
    private val eventRepository: EventsRepository,
    private val eventReviewRepository: EventReviewRepository
) : ContainerViewModel<EventStatisticsViewState, EventStatisticsSideEffect>(
    initialState = EventStatisticsViewState(isLoading = true)
) {
    init {
        fetchPastEvents()
    }

    private fun fetchPastEvents() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            val events = eventRepository.fetchPastEventsForUser(userId)
            val reviews = events.associate { event ->
                event.id to (eventReviewRepository.fetchReview(userId, event.id) ?: EventReview(
                    eventId = event.id,
                    userId = userId,
                    rating = 0,
                    comment = null
                ))
            }
            val reviewInputs = events.associate { event ->
                event.id to ReviewInput(
                    rating = reviews[event.id]?.rating ?: 0,
                    comment = reviews[event.id]?.comment ?: "",
                    isSubmitting = false,
                    isSubmitted = reviews[event.id]?.rating != 0
                )
            }
            Log.d("EventStatisticsViewModel", "Loaded ${events.size} events for user: $userId")
            reduce {
                state.copy(
                    pastEvents = events,
                    reviews = reviews,
                    reviewInputs = reviewInputs,
                    isLoading = false,
                    error = null
                )
            }
        } catch (e: Exception) {
            Log.e("EventStatisticsViewModel", "Error fetching events: ${e.message}")
            reduce { state.copy(isLoading = false, error = "Ошибка загрузки данных") }
            postSideEffect(EventStatisticsSideEffect.ShowError("Ошибка загрузки данных"))
        }
    }

    fun refreshEvents() = intent {
        fetchPastEvents()
    }

    fun onRatingChanged(eventId: String, rating: Int) = intent {
        reduce {
            state.copy(
                reviewInputs = state.reviewInputs.toMutableMap().apply {
                    this[eventId] = (this[eventId] ?: ReviewInput()).copy(rating = rating)
                }
            )
        }
    }

    fun onCommentChanged(eventId: String, comment: String) = intent {
        reduce {
            state.copy(
                reviewInputs = state.reviewInputs.toMutableMap().apply {
                    this[eventId] = (this[eventId] ?: ReviewInput()).copy(comment = comment)
                }
            )
        }
    }

    fun onSubmitReview(eventId: String) = intent {
        val reviewInput = state.reviewInputs[eventId] ?: return@intent
        if (reviewInput.rating !in 1..5) {
            postSideEffect(EventStatisticsSideEffect.ShowError("Пожалуйста, выберите оценку от 1 до 5"))
            return@intent
        }
        reduce {
            state.copy(
                reviewInputs = state.reviewInputs.toMutableMap().apply {
                    this[eventId] = reviewInput.copy(isSubmitting = true)
                }
            )
        }
        try {
            val review = EventReview(
                userId = userId,
                eventId = eventId,
                rating = reviewInput.rating,
                comment = reviewInput.comment.takeIf { it.isNotBlank() },
                createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
            val success = eventReviewRepository.submitReview(review)
            if (success) {
                reduce {
                    state.copy(
                        reviews = state.reviews.toMutableMap().apply {
                            this[eventId] = review
                        },
                        reviewInputs = state.reviewInputs.toMutableMap().apply {
                            this[eventId] = reviewInput.copy(isSubmitting = false, isSubmitted = true)
                        }
                    )
                }
            } else {
                postSideEffect(EventStatisticsSideEffect.ShowError("Не удалось отправить отзыв"))
                reduce {
                    state.copy(
                        reviewInputs = state.reviewInputs.toMutableMap().apply {
                            this[eventId] = reviewInput.copy(isSubmitting = false)
                        }
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("EventStatisticsViewModel", "Error submitting review: ${e.message}")
            postSideEffect(EventStatisticsSideEffect.ShowError("Ошибка: ${e.message}"))
            reduce {
                state.copy(
                    reviewInputs = state.reviewInputs.toMutableMap().apply {
                        this[eventId] = reviewInput.copy(isSubmitting = false)
                    }
                )
            }
        }
    }

    fun onEditReview(eventId: String) = intent {
        val reviewInput = state.reviewInputs[eventId] ?: return@intent
        if (reviewInput.rating !in 1..5) {
            postSideEffect(EventStatisticsSideEffect.ShowError("Пожалуйста, выберите оценку от 1 до 5"))
            return@intent
        }
        reduce {
            state.copy(
                reviewInputs = state.reviewInputs.toMutableMap().apply {
                    this[eventId] = reviewInput.copy(isSubmitting = true)
                }
            )
        }
        try {
            val review = EventReview(
                userId = userId,
                eventId = eventId,
                rating = reviewInput.rating,
                comment = reviewInput.comment.takeIf { it.isNotBlank() },
                createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
            val success = eventReviewRepository.submitReview(review)
            if (success) {
                reduce {
                    state.copy(
                        reviews = state.reviews.toMutableMap().apply {
                            this[eventId] = review
                        },
                        reviewInputs = state.reviewInputs.toMutableMap().apply {
                            this[eventId] = reviewInput.copy(isSubmitting = false, isSubmitted = true)
                        }
                    )
                }
            } else {
                postSideEffect(EventStatisticsSideEffect.ShowError("Не удалось обновить отзыв"))
                reduce {
                    state.copy(
                        reviewInputs = state.reviewInputs.toMutableMap().apply {
                            this[eventId] = reviewInput.copy(isSubmitting = false)
                        }
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("EventStatisticsViewModel", "Error editing review: ${e.message}")
            postSideEffect(EventStatisticsSideEffect.ShowError("Ошибка: ${e.message}"))
            reduce {
                state.copy(
                    reviewInputs = state.reviewInputs.toMutableMap().apply {
                        this[eventId] = reviewInput.copy(isSubmitting = false)
                    }
                )
            }
        }
    }

    fun onDeleteReview(eventId: String) = intent {
        reduce { state.copy(isLoading = true) }
        try {
            val success = eventReviewRepository.deleteReview(userId, eventId)
            if (success) {
                reduce {
                    state.copy(
                        reviews = state.reviews.toMutableMap().apply {
                            remove(eventId)
                        },
                        reviewInputs = state.reviewInputs.toMutableMap().apply {
                            this[eventId] = ReviewInput()
                        },
                        isLoading = false
                    )
                }
            } else {
                postSideEffect(EventStatisticsSideEffect.ShowError("Не удалось удалить отзыв"))
                reduce { state.copy(isLoading = false) }
            }
        } catch (e: Exception) {
            Log.e("EventStatisticsViewModel", "Error deleting review: ${e.message}")
            postSideEffect(EventStatisticsSideEffect.ShowError("Ошибка: ${e.message}"))
            reduce { state.copy(isLoading = false) }
        }
    }
//    private fun fetchPastEvents() = intent {
//        reduce { state.copy(isLoading = true, error = null) }
//        try {
//            val events = eventRepository.fetchPastEventsForUser(userId)
//            val reviews = events.associate { event ->
//                event.id to (eventReviewRepository.fetchReview(userId, event.id) ?: EventReview(
//                    eventId = event.id,
//                    userId = userId,
//                    rating = 0,
//                    comment = null
//                ))
//            }
//            val reviewInputs = events.associate { event ->
//                event.id to ReviewInput(
//                    rating = reviews[event.id]?.rating ?: 0,
//                    comment = reviews[event.id]?.comment ?: "",
//                    isSubmitting = false,
//                    isSubmitted = reviews[event.id]?.rating != 0 // Отзыв отправлен, если рейтинг не 0
//                )
//            }
//            reduce {
//                state.copy(
//                    pastEvents = events,
//                    reviews = reviews,
//                    reviewInputs = reviewInputs,
//                    isLoading = false,
//                    error = null
//                )
//            }
//        } catch (e: Exception) {
//            Log.e("EventStatisticsViewModel", "Error fetching events: ${e.message}")
//            reduce { state.copy(isLoading = false, error = "Ошибка загрузки данных") }
//            postSideEffect(EventStatisticsSideEffect.ShowError("Ошибка загрузки данных"))
//        }
//    }
//
//    fun refreshEvents() = intent {
//        fetchPastEvents()
//    }
//
//    fun onRatingChanged(eventId: String, rating: Int) = intent {
//        reduce {
//            state.copy(
//                reviewInputs = state.reviewInputs.toMutableMap().apply {
//                    this[eventId] = (this[eventId] ?: ReviewInput()).copy(rating = rating)
//                }
//            )
//        }
//    }
//
//    fun onCommentChanged(eventId: String, comment: String) = intent {
//        reduce {
//            state.copy(
//                reviewInputs = state.reviewInputs.toMutableMap().apply {
//                    this[eventId] = (this[eventId] ?: ReviewInput()).copy(comment = comment)
//                }
//            )
//        }
//    }
//
//    fun onSubmitReview(eventId: String) = intent {
//        val reviewInput = state.reviewInputs[eventId] ?: return@intent
//        if (reviewInput.rating !in 1..5) {
//            postSideEffect(EventStatisticsSideEffect.ShowError("Пожалуйста, выберите оценку от 1 до 5"))
//            return@intent
//        }
//        reduce {
//            state.copy(
//                reviewInputs = state.reviewInputs.toMutableMap().apply {
//                    this[eventId] = reviewInput.copy(isSubmitting = true)
//                }
//            )
//        }
//        try {
//            val success = eventReviewRepository.submitReview(
//                EventReview(
//                    userId = userId,
//                    eventId = eventId,
//                    rating = reviewInput.rating,
//                    comment = reviewInput.comment,
//                    createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
//                )
//            )
//            if (success) {
//                reduce {
//                    state.copy(
//                        reviewInputs = state.reviewInputs.toMutableMap().apply {
//                            this[eventId] = reviewInput.copy(isSubmitting = false, isSubmitted = true)
//                        }
//                    )
//                }
//            } else {
//                postSideEffect(EventStatisticsSideEffect.ShowError("Не удалось отправить отзыв"))
//            }
//        } catch (e: Exception) {
//            postSideEffect(EventStatisticsSideEffect.ShowError("Ошибка: ${e.message}"))
//        } finally {
//            reduce {
//                state.copy(
//                    reviewInputs = state.reviewInputs.toMutableMap().apply {
//                        this[eventId] = reviewInput.copy(isSubmitting = false)
//                    }
//                )
//            }
//        }
//    }
//
//    fun onEditReview(eventId: String) = intent {
//        val reviewInput = state.reviewInputs[eventId] ?: return@intent
//        reduce {
//            state.copy(
//                reviewInputs = state.reviewInputs.toMutableMap().apply {
//                    this[eventId] = reviewInput.copy(isSubmitting = true)
//                }
//            )
//        }
//        try {
//            val success = eventReviewRepository.submitReview(
//                EventReview(
//                    userId = userId,
//                    eventId = eventId,
//                    rating = reviewInput.rating,
//                    comment = reviewInput.comment,
//                    createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
//                )
//            )
//            if (success) {
//                reduce {
//                    state.copy(
//                        reviewInputs = state.reviewInputs.toMutableMap().apply {
//                            this[eventId] = reviewInput.copy(isSubmitting = false, isSubmitted = true)
//                        }
//                    )
//                }
//            } else {
//                postSideEffect(EventStatisticsSideEffect.ShowError("Не удалось обновить отзыв"))
//            }
//        } catch (e: Exception) {
//            postSideEffect(EventStatisticsSideEffect.ShowError("Ошибка: ${e.message}"))
//        } finally {
//            reduce {
//                state.copy(
//                    reviewInputs = state.reviewInputs.toMutableMap().apply {
//                        this[eventId] = reviewInput.copy(isSubmitting = false)
//                    }
//                )
//            }
//        }
//    }
//
//    fun onDeleteReview(eventId: String) = intent {
//        reduce { state.copy(isLoading = true) }
//        try {
//            // Предполагаем, что репозиторий поддерживает удаление
//            val success = eventReviewRepository.deleteReview(userId, eventId)
//            if (success) {
//                reduce {
//                    state.copy(
//                        reviewInputs = state.reviewInputs.toMutableMap().apply {
//                            this[eventId] = ReviewInput()
//                        },
//                        isLoading = false
//                    )
//                }
//            } else {
//                postSideEffect(EventStatisticsSideEffect.ShowError("Не удалось удалить отзыв"))
//            }
//        } catch (e: Exception) {
//            postSideEffect(EventStatisticsSideEffect.ShowError("Ошибка: ${e.message}"))
//        } finally {
//            reduce { state.copy(isLoading = false) }
//        }
//    }

    fun formatEventDate(isoDate: String): String {
        val instant = Instant.parse(isoDate)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = when (dateTime.monthNumber) {
            1 -> "января"
            2 -> "февраля"
            3 -> "марта"
            4 -> "апреля"
            5 -> "мая"
            6 -> "июня"
            7 -> "июля"
            8 -> "августа"
            9 -> "сентября"
            10 -> "октября"
            11 -> "ноября"
            12 -> "декабря"
            else -> ""
        }
        return "${dateTime.dayOfMonth} $month ${dateTime.year}, ${dateTime.hour}:${
            dateTime.minute.toString().padStart(2, '0')
        }"
    }
}