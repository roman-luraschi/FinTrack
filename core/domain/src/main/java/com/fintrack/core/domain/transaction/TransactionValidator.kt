package com.fintrack.core.domain.transaction

import com.fintrack.core.domain.common.DomainResult
import java.math.BigDecimal

object TransactionValidator {
    fun validateManualEntry(
        description: String,
        amount: BigDecimal,
    ): DomainResult<Unit> {
        if (description.isBlank()) return DomainResult.Error("La descripción es obligatoria")
        if (amount <= BigDecimal.ZERO) return DomainResult.Error("El monto debe ser mayor a cero")
        return DomainResult.Success(Unit)
    }
}
