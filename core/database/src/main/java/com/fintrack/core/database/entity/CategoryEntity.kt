package com.fintrack.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "categories",
    indices = [Index(value = ["parentId"]), Index(value = ["deletedAt"])],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentId: Long? = null,
    val iconName: String? = null,
    val colorHex: String? = null,
    val isSystem: Boolean = false,
    val sortOrder: Int = 0,
    val deletedAt: Instant? = null,
)
