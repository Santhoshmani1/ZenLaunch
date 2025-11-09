package com.zenlauncher.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zenlauncher.data.db.entities.FavouriteEntity

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favourites")
    suspend fun getFavourites(): List<FavouriteEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavouriteEntity): Long

    @Delete
    suspend fun deleteFavorite(favorite: FavouriteEntity)

    @Update
    suspend fun updateFavorite(favorite: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE packageName = :pkg AND className = :cls")
    suspend fun deleteByApp(pkg: String, cls: String)

    @Query("UPDATE favourites SET label = :newLabel WHERE packageName = :pkg AND className = :cls")
    suspend fun renameFavorite(pkg: String, cls: String, newLabel: String)

    @Query("DELETE FROM favourites")
    suspend fun clearAll()

}
