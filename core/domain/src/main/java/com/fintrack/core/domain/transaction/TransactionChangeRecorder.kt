package com.fintrack.core.domain.transaction

import com.fintrack.core.domain.model.ChangeReason
import com.fintrack.core.domain.model.Transaction
import com.fintrack.core.domain.model.TransactionChange
import java.time.Instant

object TransactionChangeRecorder {
    fun recordUserEdit(previous: Transaction, updated: Transaction, now: Instant): List<TransactionChange> =
        buildList {
            if (previous.amount != updated.amount) {
                add(change(updated.id, "amount", previous.amount.toPlainString(), updated.amount.toPlainString(), now))
            }
            if (previous.description != updated.description) {
                add(change(updated.id, "description", previous.description, updated.description, now))
            }
            if (previous.categoryId != updated.categoryId) {
                add(
                    change(
                        updated.id,
                        "categoryId",
                        previous.categoryId?.toString(),
                        updated.categoryId?.toString(),
                        now,
                    ),
                )
            }
            if (previous.accountId != updated.accountId) {
                add(
                    change(
                        updated.id,
                        "accountId",
                        previous.accountId.toString(),
                        updated.accountId.toString(),
                        now,
                    ),
                )
            }
            if (previous.transactionDate != updated.transactionDate) {
                add(
                    change(
                        updated.id,
                        "transactionDate",
                        previous.transactionDate.toString(),
                        updated.transactionDate.toString(),
                        now,
                    ),
                )
            }
        }

    private fun change(
        transactionId: Long,
        field: String,
        old: String?,
        new: String?,
        now: Instant,
    ) = TransactionChange(
        transactionId = transactionId,
        fieldName = field,
        oldValue = old,
        newValue = new,
        changedAt = now,
        changeReason = ChangeReason.USER_EDIT,
    )
}
