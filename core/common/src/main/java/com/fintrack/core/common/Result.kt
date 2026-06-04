package com.fintrack.core.common

import com.fintrack.core.domain.common.DomainResult

typealias Result<T> = DomainResult<T>

inline fun <T> Result<T>.getOrNull(): T? = when (this) {
    is DomainResult.Success -> data
    is DomainResult.Error -> null
}

inline fun <T> Result<T>.getOrThrow(): T = when (this) {
    is DomainResult.Success -> data
    is DomainResult.Error -> throw cause ?: IllegalStateException(message)
}
