package com.example.canteenapp

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout.LayoutParams
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.canteenapp.databinding.ActivityMainBinding
import com.example.canteenapp.utils.betterNavigate
import com.example.canteenapp.utils.createSnack
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private val sharedPrefsId: String = "sharedPrefs"
    private lateinit var sharedPreferences: SharedPreferences
    private var userRole: String? = null

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var view: View

    private var roleIcons: HashMap<String, Int> = hashMapOf(
        "admin" to R.drawable.ic_round_person_24,
        "cook" to R.drawable.ic_round_restaurant_menu_24,
        "teacher" to R.drawable.ic_round_menu_book_24,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        view = findViewById<View>(android.R.id.content).rootView

        val isAuth = intent.getStringExtra("auth").toString()

        if (isAuth == "yes") {
            createSnack(
                view, "Успешный вход!",
                Snackbar.LENGTH_LONG,
                Color.parseColor("#D8FFDC"),
                Color.parseColor("#0FD42F")
            )
        }

        sharedPreferences = getSharedPreferences(
            sharedPrefsId,
            Context.MODE_PRIVATE
        )
        userRole = sharedPreferences.getString("role", "")

        val navView: BottomNavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_activity_main)

        navView.setupWithNavController(navController)

        roleIcons[userRole]?.let { binding.specialButton.setImageResource(it) }



        when (userRole) {
            "cook" -> {
                binding.specialButton.setOnClickListener {
                    setCookFab(navView)
                }
            }
            "admin" -> {
                binding.specialButton.setOnClickListener {
                    setAdminFab(navView)
                }
            }
            "teacher" -> {
                binding.specialButton.setOnClickListener {
                    setTeacherFab(navView)
                }
            }
            else -> {
                binding.specialButton.setOnClickListener {
                    navigateTo(R.id.navigation_menu)
                }
            }
        }
    }

    private fun setCookFab(navView: BottomNavigationView) {
        val popupView = layoutInflater.inflate(R.layout.two_options_button_popup, null)
        val width = LayoutParams.WRAP_CONTENT
        val height = LayoutParams.WRAP_CONTENT

        val popupWindow = PopupWindow(popupView, width, height, true)
        popupWindow.animationStyle = R.style.ZoomAnimation
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 300)

        val firstButton = popupView.findViewById<Button>(R.id.firstButton)
        val secondButton = popupView.findViewById<Button>(R.id.secondButton)

        firstButton.text = "Все блюда"
        secondButton.text = "Все заявки"

        firstButton.setOnClickListener {
            popupNavigateTo(navView, popupWindow, R.id.goToAllDishes)
        }

        secondButton.setOnClickListener {
            popupNavigateTo(navView, popupWindow, R.id.goToRequests)
        }

        popupView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        popupWindow.dismiss()
                        return true
                    }
                }

                return v?.onTouchEvent(event) ?: true
            }
        })
    }

    private fun setAdminFab(navView: BottomNavigationView) {
        navigateToUnsetDirection(R.id.toAdminPanel, navView)
    }

    private fun setTeacherFab(navView: BottomNavigationView) {
        navigateToUnsetDirection(R.id.toClassRequests, navView)
    }

    private fun popupNavigateTo(
        navView: BottomNavigationView,
        popupWindow: PopupWindow,
        destinationId: Int
    ) {
        navigateToUnsetDirection(destinationId, navView)
        popupWindow.dismiss()
    }

    private fun navigateToUnsetDirection(
        destinationId: Int,
        navView: BottomNavigationView
    ) {
        navigateTo(destinationId)
        removeSelection(navView)
    }

    private fun navigateTo(destinationId: Int) {
        betterNavigate(navController, destinationId)
    }

    private fun removeSelection(view: BottomNavigationView) {
        val menu = view.menu
        menu.findItem(R.id.placeholder).isChecked = true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }
}