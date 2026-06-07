package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val fileId: String,
    val fileName: String,
    val localPath: String,
    val fileSize: Double,
    val dateString: String,
    val timestamp: Long,
    val lastOpenedPage: Int = 0,
    val bookmarkPages: String = "" // Comma-separated page numbers
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val fileId: String,
    val timestamp: Long
)

@Entity(tableName = "recent_viewed")
data class RecentEntity(
    @PrimaryKey val fileId: String,
    val timestamp: Long
)

@Entity(tableName = "material_requests")
data class MaterialRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val details: String,
    val timestamp: Long
)
