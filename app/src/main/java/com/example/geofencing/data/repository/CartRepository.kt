package com.example.geofencing.data.repository

import com.example.geofencing.data.model.CartPage

interface CartRepository {
    // GET /sites/{siteId}/carts - 페이지네이션된 카트 목록.
    suspend fun getCarts(siteId: Int, page: Int? = null, limit: Int? = null): CartPage
}
