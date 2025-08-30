package com.zenlauncher.data.db.entities


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "renamed_apps")
data class RenamedAppEntity(
    @PrimaryKey val packageName: String,
    val className: String,
    val customLabel: String
)
