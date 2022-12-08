package com.example.canteenapp.ui.menu.secondary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MenuSecondaryViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Это меню!"
    }
    val text: LiveData<String> = _text
}