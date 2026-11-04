package com.droidspec.core.model

data class ScreenState<T>(
    val loading: Boolean = true,
    val data: T? = null,
    val error: String? = null
)
