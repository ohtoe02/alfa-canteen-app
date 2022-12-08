package com.example.canteenapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.canteenapp.databinding.ActivityMainBinding
import com.example.canteenapp.utils.createSnack
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var view: View

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        view = findViewById<View>(android.R.id.content).rootView

        val isAuth = intent.getStringExtra("auth").toString()

        if (isAuth == "yes") {
            createSnack(view, "Успешный вход!",
                Snackbar.LENGTH_LONG,
                Color.parseColor("#D8FFDC"),
                Color.parseColor("#0FD42F")
            )
        }

        val navView: BottomNavigationView = binding.navView

//        val database = Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/")
//        val myRef = database.getReference("message")
//
//        myRef.setValue("Hello, World!")
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_family, R.id.navigation_menu, R.id.navigation_order, R.id.navigation_settings
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}