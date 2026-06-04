package com.fintrack.core.domain.model

import java.time.Instant

data class Category(
    val id: Long = 0,
    val name: String,
    val parentId: Long? = null,
    val iconName: String? = null,
    val colorHex: String? = null,
    val isSystem: Boolean = false,
    val sortOrder: Int = 0,
    val deletedAt: Instant? = null,
)
