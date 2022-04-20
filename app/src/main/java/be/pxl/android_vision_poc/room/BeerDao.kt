package be.pxl.android_vision_poc.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BeerDao {
    @Query("SELECT * FROM favorite_beer_table")
    fun getBeers(): Flow<List<FavoriteBeerModel>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favoriteBeer: FavoriteBeerModel)
}