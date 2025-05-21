package ru.walkAndTalk.ui.screens.main.search

import UserSortType
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.repository.RemoteUsersRepository

class SearchViewModel(
    private val usersRepository: RemoteUsersRepository
) : ViewModel(), ContainerHost<SearchViewState, SearchSideEffect> {

    override val container: Container<SearchViewState, SearchSideEffect> = container(SearchViewState())
    private var searchJob: Job? = null

    init {
        loadUsers()
    }

    private fun loadUsers() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            val users = usersRepository.fetchAll()
            reduce { state.copy(users = users, isLoading = false) }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, error = e.message) }
        }
    }

    fun onSortSelected(sortType: UserSortType) = intent {
        val sortedUsers = when (sortType) {
            UserSortType.NameAscending -> state.users.sortedBy { it.name }
            UserSortType.NameDescending -> state.users.sortedByDescending { it.name }
        }
        reduce { state.copy(users = sortedUsers) }
    }

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

    fun onClearQuery() = intent {
        reduce { state.copy(searchQuery = "") }
        loadUsers() // Сбрасываем поиск, загружая всех пользователей
    }

    fun onUserClick(userId: String) = intent {
        postSideEffect(SearchSideEffect.NavigateToProfile(userId))
    }
}