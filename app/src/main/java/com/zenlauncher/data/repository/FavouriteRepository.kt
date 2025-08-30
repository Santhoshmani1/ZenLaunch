package com.zenlauncher.data.repository

import com.zenlauncher.data.db.dao.FavoriteDao
import com.zenlauncher.data.db.entities.FavoriteEntity

class FavoritesRepository(private val dao: FavoriteDao) {
    suspend fun getFavorites() = dao.getFavorites()
    suspend fun addFavorite(entity: FavoriteEntity) = dao.insertFavorite(entity)
    suspend fun deleteFavorite(entity: FavoriteEntity) = dao.deleteFavorite(entity)
}
