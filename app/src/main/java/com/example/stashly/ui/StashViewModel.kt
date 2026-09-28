package com.example.stashly.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stashly.data.StashDatabase
import com.example.stashly.data.StashItem
import com.example.stashly.data.StashRepository
import com.example.stashly.data.StashType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StashViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StashRepository
    private val _currentUserId = MutableStateFlow("")

    init {
        val dao = StashDatabase.getDatabase(application).stashDao()
        repository = StashRepository(dao)
    }

    fun initUserSession(userId: String) {
        if (userId.isNotBlank() && userId != _currentUserId.value) {
            _currentUserId.value = userId
            repository.startRemoteSync(userId, viewModelScope)
        }
    }

    fun clearSession() {
        _currentUserId.value = ""
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow<StashType?>(null)
    val selectedType: StateFlow<StashType?> = _selectedType.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _showArchived = MutableStateFlow(false)
    val showArchived: StateFlow<Boolean> = _showArchived.asStateFlow()

    private val allItems = repository.getAllItems()

    val allTags: StateFlow<List<String>> = combine(allItems) { items ->
        items[0].flatMap { it.tags }.distinct().sorted()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val items: StateFlow<List<StashItem>> = combine(
        allItems,
        _currentUserId,
        _searchQuery,
        _selectedType,
        _selectedTag,
        _showArchived
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val itemsList = flows[0] as List<StashItem>
        val userId = flows[1] as String
        val query = flows[2] as String
        @Suppress("UNCHECKED_CAST")
        val type = flows[3] as StashType?
        @Suppress("UNCHECKED_CAST")
        val tag = flows[4] as String?
        val isArchived = flows[5] as Boolean

        itemsList.filter { item ->
            val userMatches = if (userId.isNotBlank()) item.userId == userId || item.userId.isBlank() else true
            val archiveMatches = item.isArchived == isArchived
            val typeMatches = type == null || item.type == type
            val tagMatches = tag == null || item.tags.contains(tag)
            val searchMatches = if (query.isBlank()) true else {
                val q = query.trim().lowercase()
                item.title.lowercase().contains(q) ||
                        item.content.lowercase().contains(q) ||
                        item.url?.lowercase()?.contains(q) == true ||
                        item.tags.any { it.lowercase().contains(q) }
            }

            userMatches && archiveMatches && typeMatches && tagMatches && searchMatches
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectType(type: StashType?) {
        _selectedType.value = type
    }

    fun selectTag(tag: String?) {
        _selectedTag.value = if (_selectedTag.value == tag) null else tag
    }

    fun setShowArchived(show: Boolean) {
        _showArchived.value = show
    }

    fun saveItem(
        id: Long = 0,
        type: StashType,
        title: String,
        content: String,
        url: String?,
        tags: List<String>
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val cleanTags = tags.map { it.trim().removePrefix("#") }.filter { it.isNotEmpty() }
            val userId = _currentUserId.value
            if (id == 0L) {
                val newItem = StashItem(
                    userId = userId,
                    type = type,
                    title = title.ifBlank { "Untitled ${type.label}" },
                    content = content,
                    url = url,
                    tags = cleanTags,
                    createdAt = now,
                    updatedAt = now,
                    isArchived = false
                )
                repository.insertItem(newItem)
            } else {
                val existingItem = repository.getItemById(id)
                if (existingItem != null) {
                    val updatedItem = existingItem.copy(
                        userId = if (existingItem.userId.isBlank()) userId else existingItem.userId,
                        type = type,
                        title = title.ifBlank { "Untitled ${type.label}" },
                        content = content,
                        url = url,
                        tags = cleanTags,
                        updatedAt = now
                    )
                    repository.updateItem(updatedItem)
                }
            }
        }
    }

    fun toggleArchive(item: StashItem) {
        viewModelScope.launch {
            repository.setArchivedStatus(item.id, !item.isArchived)
        }
    }

    fun deleteItem(item: StashItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    suspend fun getItemById(id: Long): StashItem? {
        return repository.getItemById(id)
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StashViewModel::class.java)) {
                return StashViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
