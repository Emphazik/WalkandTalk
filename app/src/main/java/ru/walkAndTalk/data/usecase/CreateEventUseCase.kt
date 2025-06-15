package ru.walkAndTalk.data.usecase

import android.util.Log
import ru.walkAndTalk.data.mapper.toDto
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.repository.EventsRepository
import ru.walkAndTalk.domain.repository.FeedRepository

class CreateEventUseCase(
    private val eventsRepository: EventsRepository,
    private val feedRepository: FeedRepository
) {
    suspend operator fun invoke(event: Event): Result<Unit> {
        Log.d("CreateEventUseCase", "Входной event: $event")
        return feedRepository.createEvent(event)
    }
}