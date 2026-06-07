package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE fileId = :fileId LIMIT 1")
    suspend fun getDownloadById(fileId: String): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Query("UPDATE downloads SET lastOpenedPage = :page WHERE fileId = :fileId")
    suspend fun updateLastOpenedPage(fileId: String, page: Int)

    @Query("UPDATE downloads SET bookmarkPages = :bookmarkPages WHERE fileId = :fileId")
    suspend fun updateBookmarks(fileId: String, bookmarkPages: String)

    @Query("DELETE FROM downloads WHERE fileId = :fileId")
    suspend fun deleteDownload(fileId: String)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE fileId = :fileId)")
    fun isFavoriteFlow(fileId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE fileId = :fileId)")
    suspend fun isFavorite(fileId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE fileId = :fileId")
    suspend fun deleteFavorite(fileId: String)
}

@Dao
interface RecentDao {
    @Query("SELECT * FROM recent_viewed ORDER BY timestamp DESC LIMIT 50")
    fun getAllRecents(): Flow<List<RecentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(recent: RecentEntity)

    @Query("DELETE FROM recent_viewed WHERE fileId = :fileId")
    suspend fun deleteRecent(fileId: String)
}

@Dao
interface MaterialRequestDao {
    @Query("SELECT * FROM material_requests ORDER BY timestamp DESC")
    fun getAllRequests(): Flow<List<MaterialRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: MaterialRequestEntity)
}
