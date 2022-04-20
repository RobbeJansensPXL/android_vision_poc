package be.pxl.android_vision_poc.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope

@Database(entities = arrayOf(FavoriteBeerModel::class), version = 1, exportSchema = false)
abstract class BeerDatabase : RoomDatabase() {
    abstract fun beerDao(): BeerDao

    companion object {
        @Volatile
        private var INSTANCE: BeerDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BeerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BeerDatabase::class.java,
                    "beer_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}