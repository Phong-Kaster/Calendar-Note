package com.example.skeleton.ui.fragment.home

import com.example.skeleton.domain.model.Post

data class HomeUiState(
    val posts: List<Post> = emptyList(),
    val isRefreshing: Boolean = false,
    val refreshError: String? = null,
)