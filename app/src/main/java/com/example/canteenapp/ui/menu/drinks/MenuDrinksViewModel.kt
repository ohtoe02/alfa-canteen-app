package com.example.canteenapp.ui.menu.drinks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MenuDrinksViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Это меню!"
    }
    val text: LiveData<String> = _text
}