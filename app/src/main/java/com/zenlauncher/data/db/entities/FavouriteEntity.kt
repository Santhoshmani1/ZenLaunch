package com.zenlauncher.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favourites",
    indices = [Index(value = ["packageName", "className"], unique = true)]
)
data class FavouriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val packageName: String,
    val className: String
)
