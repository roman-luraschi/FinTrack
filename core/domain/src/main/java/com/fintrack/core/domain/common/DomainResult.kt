package com.fintrack.core.domain.common

sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : DomainResult<Nothing>()
}

inline fun <T> DomainResult<T>.getOrNull(): T? = when (this) {
    is DomainResult.Success -> data
    is DomainResult.Error -> null
}

inline fun <T> DomainResult<T>.getOrThrow(): T = when (this) {
    is DomainResult.Success -> data
    is DomainResult.Error -> throw cause ?: IllegalStateException(message)
}
