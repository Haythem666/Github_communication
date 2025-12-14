package com.example.lab3_github_communication.data.api

import com.example.lab3_github_communication.data.model.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GitHubApiService {
    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("sort") sort: String = "stars",
        @Query("order") order: String = "desc"
    ): SearchResponse
}