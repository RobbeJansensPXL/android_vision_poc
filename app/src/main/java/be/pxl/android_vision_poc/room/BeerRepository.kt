package be.pxl.android_vision_poc.room

import kotlinx.coroutines.flow.Flow

class BeerRepository(private val beerDao: BeerDao) {
    val allBeers: Flow<List<FavoriteBeerModel>> = beerDao.getBeers()

    suspend fun insert(favoriteBeerModel: FavoriteBeerModel) {
        beerDao.insert(favoriteBeerModel)
    }
}