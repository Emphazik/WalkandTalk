package ru.walkAndTalk.ui.screens.main.search

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.repository.RemoteUsersRepository

class SearchViewModel(
    private val usersRepository: RemoteUsersRepository
) : ViewModel(), ContainerHost<SearchViewState, SearchSideEffect> {

    override val container: Container<SearchViewState, SearchSideEffect> = container(SearchViewState())

    fun onSearchQueryChange(query: String) = intent {
        reduce { state.copy(isLoading = true, error = null, searchQuery = query) }
        try {
            val users = if (query.isNotEmpty()) {
                usersRepository.searchUsers(query) // Предполагаем метод поиска
            } else {
                emptyList()
            }
            reduce { state.copy(users = users, isLoading = false) }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, error = e.message) }
        }
    }

    fun onUserClick(userId: String) = intent {
        postSideEffect(SearchSideEffect.NavigateToProfile(userId))
    }
}