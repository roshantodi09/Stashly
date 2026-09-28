package com.example.stashly.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class StashType(val label: String) {
    NOTE("Note"),
    LINK("Link"),
    SNIPPET("Snippet"),
    IMAGE("Image")
}

@Entity(tableName = "stash_items")
data class StashItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: StashType,
    val title: String,
    val content: String,
    val url: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)
