package com.example.geofencing.data.repository

import com.example.geofencing.data.model.CartPage
import com.example.geofencing.data.remote.GeofencingApi
import com.example.geofencing.data.remote.toDomain
import com.example.geofencing.data.remote.unwrap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val api: GeofencingApi
) : CartRepository {

    override suspend fun getCarts(siteId: Int, page: Int?, limit: Int?): CartPage =
        api.getCarts(siteId, page, limit).unwrap().toDomain()
}
