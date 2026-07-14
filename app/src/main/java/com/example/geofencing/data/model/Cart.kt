package com.example.geofencing.data.model

// GET /sites/{siteId}/carts 응답의 도메인 표현.
data class Cart(
    val id: Int,
    val name: String,
    val violating: Boolean
)

data class CartPage(
    val carts: List<Cart>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)
