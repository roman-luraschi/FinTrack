package com.fintrack.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fintrack.core.domain.model.AccountType
import java.time.Instant

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["deletedAt"])],
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val currency: String = "ARS",
    val colorHex: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
)
