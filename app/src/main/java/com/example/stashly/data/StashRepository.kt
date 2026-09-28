package com.example.stashly.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.math.abs

class StashRepository(
    private val dao: StashDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private var syncListenerRegistration: ListenerRegistration? = null

    fun getAllItems(): Flow<List<StashItem>> = dao.getAllItems()

    suspend fun getItemById(id: Long): StashItem? = dao.getItemById(id)

    /**
     * Start real-time Firestore listener for cross-device sync scoped to current userId.
     */
    fun startRemoteSync(userId: String, scope: CoroutineScope) {
        if (userId.isBlank()) return

        // Unregister any previous listener to prevent duplicate background loops
        syncListenerRegistration?.remove()

        try {
            syncListenerRegistration = firestore.collection("stash_items")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch(Dispatchers.IO) {
                        for (doc in snapshot.documents) {
                            val typeStr = doc.getString("type") ?: "NOTE"
                            val type = try { StashType.valueOf(typeStr) } catch (e: Exception) { StashType.NOTE }
                            val tagsList = (doc.get("tags") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList()

                            // Compute stable deterministic ID from document ID to prevent duplicate infinite inserts
                            val stableId = doc.getLong("id") ?: abs(doc.id.hashCode().toLong()).let { if (it == 0L) 1L else it }

                            val item = StashItem(
                                id = stableId,
                                userId = doc.getString("userId") ?: userId,
                                type = type,
                                title = doc.getString("title") ?: "",
                                content = doc.getString("content") ?: "",
                                url = doc.getString("url"),
                                tags = tagsList,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                                isArchived = doc.getBoolean("isArchived") ?: false
                            )
                            // Direct DAO insert to update local Room DB
                            dao.insertItem(item)
                        }
                    }
                }
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun insertItem(item: StashItem): Long {
        val insertedId = dao.insertItem(item)
        val finalItem = if (item.id == 0L) item.copy(id = insertedId) else item
        syncToRemote(finalItem)
        return insertedId
    }

    suspend fun updateItem(item: StashItem) {
        dao.updateItem(item)
        syncToRemote(item)
    }

    suspend fun deleteItem(item: StashItem) {
        dao.deleteItem(item)
        deleteFromRemote(item)
    }

    suspend fun setArchivedStatus(id: Long, isArchived: Boolean, updatedAt: Long = System.currentTimeMillis()) {
        dao.setArchivedStatus(id, isArchived, updatedAt)
        val item = dao.getItemById(id)
        if (item != null) {
            syncToRemote(item.copy(isArchived = isArchived, updatedAt = updatedAt))
        }
    }

    private fun syncToRemote(item: StashItem) {
        if (item.userId.isBlank()) return
        try {
            val docRef = firestore.collection("stash_items").document("${item.userId}_${item.id}")
            val map = hashMapOf(
                "id" to item.id,
                "userId" to item.userId,
                "type" to item.type.name,
                "title" to item.title,
                "content" to item.content,
                "url" to item.url,
                "tags" to item.tags,
                "createdAt" to item.createdAt,
                "updatedAt" to item.updatedAt,
                "isArchived" to item.isArchived
            )
            docRef.set(map, SetOptions.merge())
        } catch (e: Exception) {
            // Saved locally in Room
        }
    }

    private fun deleteFromRemote(item: StashItem) {
        if (item.userId.isBlank()) return
        try {
            firestore.collection("stash_items")
                .document("${item.userId}_${item.id}")
                .delete()
        } catch (e: Exception) {
            // Deleted locally
        }
    }
}
