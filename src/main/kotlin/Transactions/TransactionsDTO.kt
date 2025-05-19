package com.project.database.transactions

import kotlinx.serialization.Serializable

@Serializable
data class TransactionDTO(
    val id: Int = 0,
    val userEmail: String,
    val amount: Int,
    val description: String,
    val timestamp: Long
)

