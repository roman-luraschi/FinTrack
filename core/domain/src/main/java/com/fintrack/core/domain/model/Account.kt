package com.fintrack.core.domain.model

import java.time.Instant

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val currency: String = "ARS",
    val colorHex: String? = null,
    val isDefault: Boolean = false,
    val integrationProvider: IntegrationProvider? = null,
    val externalAccountId: String? = null,
    val notificationListenerEnabled: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
)
