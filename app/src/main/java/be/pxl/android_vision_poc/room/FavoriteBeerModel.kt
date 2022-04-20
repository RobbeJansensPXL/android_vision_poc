package be.pxl.android_vision_poc.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_beer_table")
class FavoriteBeerModel (@PrimaryKey @ColumnInfo(name = "beer") val beer: String)