package ru.walkAndTalk.ui.screens.main.search


import UserSortType
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository

class SearchViewModel(
    private val usersRepository: RemoteUsersRepository,
    private val currentUserId: String // Добавляем currentUserId как параметр
) : ViewModel(), ContainerHost<SearchViewState, SearchSideEffect>, KoinComponent {

    override val container: Container<SearchViewState, SearchSideEffect> = container(SearchViewState())
    private var searchJob: Job? = null
    private var allUsers: List<User> = emptyList()
    private val userCache = mutableMapOf<String, User>()
    private val chatsRepository: ChatsRepository by inject()

    init {
        loadUsers()
    }

    private fun loadUsers() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            allUsers = usersRepository.fetchAll()
            allUsers.forEach { user ->
                userCache[user.id] = user
            }
            println("SearchViewModel: Loaded ${allUsers.size} users, cached ${userCache.size} users")
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
        userCache.clear()
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
        reduce { state.copy(isLoading = true, error = null, searchQuery = query, isSearchActive = query.isNotEmpty()) }
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
        reduce { state.copy(searchQuery = "", isSearchActive = false) }
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
        try {
            val chat = chatsRepository.findOrCreatePrivateChat(currentUserId, userId)
            println("SearchViewModel: Chat created or found for userId=$userId, chatId=${chat.id}")
            postSideEffect(SearchSideEffect.NavigateToMessage(chat.id))
        } catch (e: Exception) {
            println("SearchViewModel: Error creating/finding chat for userId=$userId, error=${e.message}")
            postSideEffect(SearchSideEffect.ShowError(e.message ?: "Ошибка создания чата"))
        }
    }

    fun toggleSearch() = intent {
        reduce { state.copy(isSearchActive = !state.isSearchActive, searchQuery = if (!state.isSearchActive) "" else state.searchQuery) }
    }

    private fun applySort(users: List<User>, query: String): List<User> {
        return if (query.isEmpty()) {
            users.sortedBy { it.name.lowercase() }
        } else {
            users
        }
    }
}