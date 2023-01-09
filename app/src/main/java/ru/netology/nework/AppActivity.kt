package ru.netology.nework

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import ru.netology.nework.databinding.ActivityMainBinding

class AppActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    fun setActionBarTitle(title: String) {
        binding.mainToolbar.title = title
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

                menuInflater.inflate(R.menu.fragment_create_edit_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return true
            }
        })

        //получаем доступ к BottomNavigationView
        val navView: BottomNavigationView = findViewById(R.id.bottom_nav_view)

        //получаем доступ к фрагменту в файле xml, в который будем вставлять фрагменты, через supportFragmentManager
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        navView.setupWithNavController(navController)

        val toolbar = binding.mainToolbar
        setSupportActionBar(toolbar)

        binding.fabAddPost.setOnClickListener {
            navController.navigate(R.id.newPostFragment)
        }


        checkGoogleApiAvailability()
    }


    private fun checkGoogleApiAvailability() {
        with(GoogleApiAvailability.getInstance()) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            println(it)
        }
    }
}