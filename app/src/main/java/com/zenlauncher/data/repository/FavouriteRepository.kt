package com.zenlauncher.data.repository

import com.zenlauncher.data.db.dao.FavoriteDao
import com.zenlauncher.data.db.entities.FavouriteEntity

class FavouritesRepository(private val dao: FavoriteDao) {
    suspend fun getFavourites() = dao.getFavourites()
    suspend fun addFavorite(entity: FavouriteEntity) = dao.insertFavorite(entity)
    suspend fun deleteFavorite(entity: FavouriteEntity) = dao.deleteFavorite(entity)
}
