package com.example.canteenapp

import android.app.ActionBar.LayoutParams
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.canteenapp.databinding.ActivityLoginBinding
import com.example.canteenapp.utils.createSnack
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var view: View
    private lateinit var database: DatabaseReference
    private lateinit var allSchoolsReference: DatabaseReference
    private lateinit var schoolsAdapter: ArrayAdapter<String>
    private lateinit var allSchools: HashMap<String, String>

    private val sharedPrefs: String = "sharedPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)


        if (getAuthenticationState()) {
            val intent = Intent(this, MainActivity::class.java).putExtra("auth", "no")
            startActivity(intent)
            finish()
        }

        init()

        binding.loginButton.setOnClickListener {
            val schoolTitle: String = binding.school.text.toString()

            tryLogin(schoolTitle)
        }
    }

    private fun init() {
        setBinding()
        setView()
        setupSchoolInput()
    }

    private fun setBinding() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setView() {
        view = findViewById<View>(android.R.id.content).rootView
        setContentView(binding.root)
    }

    private fun setupSchoolInput() {
        allSchoolsReference = Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/").getReference("allSchools")
        allSchoolsReference.get().addOnSuccessListener {
            if (it.exists()) {
                allSchools = it.value as HashMap<String, String>
                val schoolInput = binding.school
                schoolInput.threshold = 1
                schoolsAdapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, allSchools.values.toMutableList())
                schoolInput.setAdapter(schoolsAdapter)
            }
        }
    }

    private fun getSchoolId(school: String): String {
        return allSchools.filterValues { it == school }.keys.firstOrNull() ?: ""
    }

    private fun tryLogin(school: String) {
        val schoolId = getSchoolId(school)

        val login: String = binding.login.text.toString()
        val password: String = binding.password.text.toString()

        if (schoolId.isNotEmpty() && login.isNotEmpty() && password.isNotEmpty()) {
            database =
                Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("schools/$schoolId/users")
            database.child(login).get().addOnSuccessListener {
                if (it.exists()) {
                    val currentPass = it.child("password").value

                    if (currentPass == password) {
                        setAuthenticationState(
                            login,
                            it.child("role").value as String,
                            schoolId,
                            it.child("family/surname").value as String
                        )
                        val intent = Intent(this, MainActivity::class.java).putExtra("auth", "yes")
                        startActivity(intent)
                        finish()
                        createSnack(view, "Успешный вход!",
                            Snackbar.LENGTH_LONG,
                            Color.parseColor("#52FF4F"),
                            Color.parseColor("#D8FFDC")
                        )
                    }

                } else {
                    createSnack(view, "Пользователь не найден",
                        Snackbar.LENGTH_LONG,
                        Color.parseColor("#FFE2E2"),
                        Color.parseColor("#FF5D5D")
                    )
                }
            }.addOnFailureListener {
                createSnack(view, "Потеряна связь",
                    Snackbar.LENGTH_LONG,
                    Color.parseColor("#FFE2E2"),
                    Color.parseColor("#FF5D5D")
                )
            }
        } else {
            createSnack(view, "Пользователь не найден",
                Snackbar.LENGTH_LONG,
                Color.parseColor("#FFE2E2"),
                Color.parseColor("#FF5D5D")
            )
//            Toast.makeText(this, "Login Failed!", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun createSnack(text: String, length: Int, bgColor: Int, textColor: Int) {
//        val snack = Snackbar
//            .make(view, text, length)
//            .setBackgroundTint(bgColor)
//            .setTextColor(textColor)
//
//        snack.show()
//    }

    private fun setAuthenticationState(login: String, role: String, school: String, surname: String) {
        val sharedPreferences: SharedPreferences = getSharedPreferences(sharedPrefs, MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putString("login", login)
        editor.putString("role", role)
        editor.putString("school", school)
        editor.putString("familySurname", surname)
        editor.putBoolean("isAuthenticated", true)

        editor.apply()
    }

    private fun getAuthenticationState():Boolean {
        val sharedPreferences: SharedPreferences = getSharedPreferences(sharedPrefs, MODE_PRIVATE)

        return sharedPreferences.getBoolean("isAuthenticated", false)
    }
}