package com.zenlauncher.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zenlauncher.data.db.dao.FavoriteDao
import com.zenlauncher.data.db.dao.RenamedAppDao
import com.zenlauncher.data.db.entities.FavouriteEntity
import com.zenlauncher.data.db.entities.RenamedAppEntity

@Database(
    entities = [FavouriteEntity::class, RenamedAppEntity::class],
    version = 2,
    exportSchema = false
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun renamedAppDao(): RenamedAppDao

    companion object {
        @Volatile private var INSTANCE: LauncherDatabase? = null

        fun getDatabase(context: Context): LauncherDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    LauncherDatabase::class.java,
                    "launcher_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
