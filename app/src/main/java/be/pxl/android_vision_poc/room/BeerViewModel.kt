package be.pxl.android_vision_poc.room

import androidx.lifecycle.*
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class BeerViewModel(private val repository: BeerRepository) : ViewModel() {
    val allBeers: LiveData<List<FavoriteBeerModel>> = repository.allBeers.asLiveData()

    fun insert(beer: FavoriteBeerModel) = viewModelScope.launch {
        repository.insert(beer)
    }

    class BeerViewModelFactory(private val repository: BeerRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BeerViewModel::class.java)) {
                return BeerViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}