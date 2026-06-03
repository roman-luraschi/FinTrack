package com.fintrack.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fintrack.core.domain.model.MatchType
import java.time.Instant

@Entity(
    tableName = "classification_rules",
    indices = [Index(value = ["pattern"]), Index(value = ["isActive"])],
)
data class ClassificationRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pattern: String,
    val matchType: MatchType,
    val categoryId: Long,
    val priority: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Instant,
)
