package com.fintrack.core.database.mapper

import com.fintrack.core.database.entity.AccountEntity
import com.fintrack.core.database.entity.CategoryEntity
import com.fintrack.core.database.entity.CategoryTotalEntity
import com.fintrack.core.database.entity.ClassificationRuleEntity
import com.fintrack.core.database.entity.LearnedMerchantCategoryEntity
import com.fintrack.core.database.entity.TransactionChangeEntity
import com.fintrack.core.database.entity.TransactionEntity
import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.model.Category
import com.fintrack.core.domain.model.CategoryTotal
import com.fintrack.core.domain.model.ClassificationRule
import com.fintrack.core.domain.model.LearnedMerchantCategory
import com.fintrack.core.domain.model.Transaction
import com.fintrack.core.domain.model.TransactionChange
import java.math.BigDecimal

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    name = name,
    type = type,
    currency = currency,
    colorHex = colorHex,
    isDefault = isDefault,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    name = name,
    type = type,
    currency = currency,
    colorHex = colorHex,
    isDefault = isDefault,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    parentId = parentId,
    iconName = iconName,
    colorHex = colorHex,
    isSystem = isSystem,
    sortOrder = sortOrder,
    deletedAt = deletedAt,
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    parentId = parentId,
    iconName = iconName,
    colorHex = colorHex,
    isSystem = isSystem,
    sortOrder = sortOrder,
    deletedAt = deletedAt,
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    externalId = externalId,
    amount = amount,
    type = type,
    description = description,
    merchantNormalized = merchantNormalized,
    categoryId = categoryId,
    subcategoryId = subcategoryId,
    classificationSource = classificationSource,
    classificationConfidence = classificationConfidence,
    needsReview = needsReview,
    source = source,
    accountId = accountId,
    transactionDate = transactionDate,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    externalId = externalId,
    amount = amount,
    type = type,
    description = description,
    merchantNormalized = merchantNormalized,
    categoryId = categoryId,
    subcategoryId = subcategoryId,
    classificationSource = classificationSource,
    classificationConfidence = classificationConfidence,
    needsReview = needsReview,
    source = source,
    accountId = accountId,
    transactionDate = transactionDate,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
)

fun ClassificationRuleEntity.toDomain(): ClassificationRule = ClassificationRule(
    id = id,
    pattern = pattern,
    matchType = matchType,
    categoryId = categoryId,
    priority = priority,
    isActive = isActive,
    createdAt = createdAt,
)

fun ClassificationRule.toEntity(): ClassificationRuleEntity = ClassificationRuleEntity(
    id = id,
    pattern = pattern,
    matchType = matchType,
    categoryId = categoryId,
    priority = priority,
    isActive = isActive,
    createdAt = createdAt,
)

fun LearnedMerchantCategoryEntity.toDomain(): LearnedMerchantCategory = LearnedMerchantCategory(
    id = id,
    merchantNormalized = merchantNormalized,
    categoryId = categoryId,
    subcategoryId = subcategoryId,
    learnedAt = learnedAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
)

fun LearnedMerchantCategory.toEntity(): LearnedMerchantCategoryEntity = LearnedMerchantCategoryEntity(
    id = id,
    merchantNormalized = merchantNormalized,
    categoryId = categoryId,
    subcategoryId = subcategoryId,
    learnedAt = learnedAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
)

fun TransactionChangeEntity.toDomain(): TransactionChange = TransactionChange(
    id = id,
    transactionId = transactionId,
    fieldName = fieldName,
    oldValue = oldValue,
    newValue = newValue,
    changedAt = changedAt,
    changeReason = changeReason,
)

fun TransactionChange.toEntity(): TransactionChangeEntity = TransactionChangeEntity(
    id = id,
    transactionId = transactionId,
    fieldName = fieldName,
    oldValue = oldValue,
    newValue = newValue,
    changedAt = changedAt,
    changeReason = changeReason,
)

fun CategoryTotalEntity.toDomain(): CategoryTotal = CategoryTotal(
    categoryId = categoryId,
    categoryName = categoryName,
    total = total,
)

fun String.toBigDecimalSafe(): BigDecimal = if (isBlank()) BigDecimal.ZERO else BigDecimal(this)
