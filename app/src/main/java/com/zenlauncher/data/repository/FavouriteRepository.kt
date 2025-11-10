package com.zenlauncher.data.repository

import com.zenlauncher.data.db.dao.FavouriteDao
import com.zenlauncher.data.db.entities.FavouriteEntity

class FavouritesRepository(private val dao: FavouriteDao) {
    suspend fun getFavourites() = dao.getFavourites()
    suspend fun addFavourite(entity: FavouriteEntity) = dao.insertFavourite(entity)
    suspend fun deleteFavourite(entity: FavouriteEntity) = dao.deleteFavourite(entity)
}
