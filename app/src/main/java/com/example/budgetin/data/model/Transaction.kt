package com.example.budgetin.data.model

data class Transaction(
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val date: String,
    val type: String,
    val categoryId: Int?,
    val goalId: Int?,
    val message: String? = null
) : java.io.Serializable