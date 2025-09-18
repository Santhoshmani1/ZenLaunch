package com.zenlauncher.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zenlauncher.data.db.entities.FavoriteEntity

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    suspend fun getFavorites(): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Update
    suspend fun updateFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE packageName = :pkg AND className = :cls")
    suspend fun deleteByApp(pkg: String, cls: String)

    @Query("UPDATE favorites SET label = :newLabel WHERE packageName = :pkg AND className = :cls")
    suspend fun renameFavorite(pkg: String, cls: String, newLabel: String)

    @Query("DELETE FROM favorites")
    suspend fun clearAll()

}
