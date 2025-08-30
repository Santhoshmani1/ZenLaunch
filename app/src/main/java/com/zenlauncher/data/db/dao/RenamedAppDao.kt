package com.zenlauncher.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenlauncher.data.db.entities.RenamedAppEntity

@Dao
interface RenamedAppDao {

    @Query("SELECT * FROM renamed_apps WHERE packageName = :pkg AND className = :cls LIMIT 1")
    suspend fun getRename(pkg: String, cls: String): RenamedAppEntity?

    @Query("SELECT * FROM renamed_apps")
    suspend fun getRenamedApps(): List<RenamedAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRename(renamedApp: RenamedAppEntity)

    @Delete
    suspend fun deleteRename(renamedApp: RenamedAppEntity)

    @Query("DELETE FROM renamed_apps WHERE packageName = :pkg AND className = :cls")
    suspend fun deleteByApp(pkg: String, cls: String)
}
