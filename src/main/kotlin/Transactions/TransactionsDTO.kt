package com.project.database.transactions

import kotlinx.serialization.Serializable


@Serializable
data class TransactionDTO(
    val senderEmail: String,      // This is camelCase
    val recipientEmail: String,   // This is camelCase
    val amount: Int,
    val description: String,
    val timestamp: Long
)
