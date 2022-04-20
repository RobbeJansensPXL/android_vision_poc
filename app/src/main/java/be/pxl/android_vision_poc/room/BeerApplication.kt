package be.pxl.android_vision_poc.room

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class BeerApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { BeerDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { BeerRepository(database.beerDao()) }
}