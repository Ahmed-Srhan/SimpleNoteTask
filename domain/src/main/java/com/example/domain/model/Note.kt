package com.example.domain.model

data class Note(
    val id: Int = 0,
    val title: String? = null,
    val content: String,
    val imageUri: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
