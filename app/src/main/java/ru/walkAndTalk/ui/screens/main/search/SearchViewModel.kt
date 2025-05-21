package ru.walkAndTalk.ui.screens.main.search

import UserSortType
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.domain.repository.RemoteUsersRepository

class SearchViewModel(
    private val usersRepository: RemoteUsersRepository
) : ViewModel(), ContainerHost<SearchViewState, SearchSideEffect> {

    override val container: Container<SearchViewState, SearchSideEffect> = container(SearchViewState())
    private var searchJob: Job? = null
    private var allUsers: List<User> = emptyList()
    private val userCache = mutableMapOf<String, User>()

    init {
        loadUsers()
    }

    private fun loadUsers() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            allUsers = usersRepository.fetchAll()
            // Заполняем кэш
            allUsers.forEach { user ->
                userCache[user.id] = user
            }
            println("SearchViewModel: Loaded ${allUsers.size} users, cached ${userCache.size} users")
            // Собираем все interestIds
            val allInterestIds = allUsers.flatMap { it.interestIds }.distinct()
            val interestNames = usersRepository.fetchInterestNames(allInterestIds)
                .zip(allInterestIds) { name, id -> id to name }
                .toMap()
            val sortedUsers = applySort(allUsers, state.searchQuery)
            reduce {
                state.copy(
                    users = sortedUsers,
                    isLoading = false,
                    interestNames = interestNames
                )
            }
        } catch (e: Exception) {
            println("SearchViewModel: LoadUsers error=${e.message}")
            reduce { state.copy(isLoading = false, error = e.message) }
            postSideEffect(SearchSideEffect.ShowError(e.message ?: "Ошибка загрузки пользователей"))
        }
    }

    fun refreshUsers() = intent {
        userCache.clear() // Очищаем кэш при обновлении
        println("SearchViewModel: Cleared cache, refreshing users")
        loadUsers()
    }

    fun onSortSelected(sortType: UserSortType) = intent {
        println("SearchViewModel: SortType=$sortType")
        val sortedUsers = when (sortType) {
            UserSortType.NameAscending -> state.users.sortedBy { it.name.lowercase() }
            UserSortType.NameDescending -> state.users.sortedByDescending { it.name.lowercase() }
        }
        reduce { state.copy(users = sortedUsers) }
    }

    fun onSearchQueryChange(query: String) = intent {
        println("SearchViewModel: SearchQuery=$query")
        reduce { state.copy(isLoading = true, error = null, searchQuery = query) }
        try {
            val users = if (query.isNotEmpty()) {
                if (query.startsWith("#") && query.length > 1) {
                    val tagQuery = query.drop(1).trim()
                    usersRepository.searchUsers("#$tagQuery")
                } else {
                    usersRepository.searchUsers(query)
                }
            } else {
                allUsers
            }
            // Обновляем кэш для новых пользователей
            users.forEach { user ->
                userCache[user.id] = user
            }
            println("SearchViewModel: Filtered ${users.size} users for query=$query, cached ${userCache.size} users")
            val sortedUsers = applySort(users, query)
            reduce { state.copy(users = sortedUsers, isLoading = false) }
        } catch (e: Exception) {
            println("SearchViewModel: Search error=${e.message}")
            reduce { state.copy(isLoading = false, error = e.message) }
            postSideEffect(SearchSideEffect.ShowError(e.message ?: "Ошибка поиска"))
        }
    }

    fun onClearQuery() = intent {
        println("SearchViewModel: ClearQuery")
        reduce { state.copy(searchQuery = "") }
        loadUsers()
    }

    fun onUserClick(userId: String) = intent {
        val cachedUser = userCache[userId]
        if (cachedUser != null) {
            println("SearchViewModel: Using cached user for userId=$userId")
            postSideEffect(SearchSideEffect.NavigateToProfile(userId))
        } else {
            try {
                val user = usersRepository.fetchById(userId)
                if (user != null) {
                    userCache[userId] = user
                    println("SearchViewModel: Fetched and cached user for userId=$userId")
                    postSideEffect(SearchSideEffect.NavigateToProfile(userId))
                } else {
                    println("SearchViewModel: User not found for userId=$userId")
                    postSideEffect(SearchSideEffect.ShowError("Пользователь не найден"))
                }
            } catch (e: Exception) {
                println("SearchViewModel: Error fetching userId=$userId, error=${e.message}")
                postSideEffect(SearchSideEffect.ShowError(e.message ?: "Ошибка загрузки профиля"))
            }
        }
    }

    fun onMessageClick(userId: String) = intent {
        postSideEffect(SearchSideEffect.NavigateToMessage(userId))
    }

    private fun applySort(users: List<User>, query: String): List<User> {
        return if (query.isEmpty()) {
            users.sortedBy { it.name.lowercase() }
        } else {
            users
        }
    }
}