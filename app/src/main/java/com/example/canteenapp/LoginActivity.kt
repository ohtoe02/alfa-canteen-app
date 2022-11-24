package com.example.canteenapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.canteenapp.databinding.ActivityLoginBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: DatabaseReference

    private val sharedPrefs: String = "sharedPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)

        if (getAuthenticationState()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.loginButton.setOnClickListener {
            val login: String = binding.login.text.toString()
            val password: String = binding.password.text.toString()

            if (login.isNotEmpty() && password.isNotEmpty()) {
                database =
                    Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/")
                        .getReference("schools/school1/users")
                database.child(login).get().addOnSuccessListener {
                    if (it.exists()) {
                        val currentPass = it.child("password").value

                        if (currentPass == password) {
                            setAuthenticationState(login, it.child("role").value as String, true)
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                            finish()
                        }

                    } else {
                        Toast.makeText(this, "User doesn't exist", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to connect!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Login Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setAuthenticationState(login: String, role: String, isAuthenticated: Boolean = false) {
        val sharedPreferences: SharedPreferences = getSharedPreferences(sharedPrefs, MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putString("login", login)
        editor.putString("role", role)
        editor.putBoolean("isAuthenticated", isAuthenticated)

        editor.apply()
    }

    private fun getAuthenticationState():Boolean {
        val sharedPreferences: SharedPreferences = getSharedPreferences(sharedPrefs, MODE_PRIVATE)

        return sharedPreferences.getBoolean("isAuthenticated", false)
    }
}