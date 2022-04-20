package be.pxl.android_vision_poc

import android.Manifest
import android.os.Bundle
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import be.pxl.android_vision_poc.databinding.ActivityMainBinding
import be.pxl.android_vision_poc.fragments.FavoritesFragment
import be.pxl.android_vision_poc.fragments.SearchFragment
import be.pxl.android_vision_poc.room.BeerApplication
import be.pxl.android_vision_poc.room.BeerViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val searchFragment = SearchFragment()
    private val favoritesFragment = FavoritesFragment()

    private lateinit var viewBinding: ActivityMainBinding

    public val beerViewModel: BeerViewModel by viewModels {
        BeerViewModel.BeerViewModelFactory((application as BeerApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        replaceFragment(searchFragment)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bnv_main)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.ic_search -> replaceFragment(searchFragment)
                R.id.ic_favorites -> replaceFragment(favoritesFragment)
            }
            true
        }
    }

    companion object {
        const val TAG = "Android Vision POC"
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).toTypedArray()
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fl_fragment_container, fragment)
        transaction.commit()
    }
}