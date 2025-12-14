package com.example.lab3_github_communication.data.model

data class SearchResponse(
    val total_count: Int,
    val items: List<Repository>?  // Nullable pour gérer les erreurs API
)

data class Repository(
    val name: String,
    val description: String?,
    val stargazers_count: Int,
    val language: String?,
    val owner: Owner
)

data class Owner(
    val login: String,
)