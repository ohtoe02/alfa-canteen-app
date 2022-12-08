package com.example.canteenapp.ui.settings.adddish

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddDishViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Добавить блюдо"
    }
    val text: LiveData<String> = _text
}