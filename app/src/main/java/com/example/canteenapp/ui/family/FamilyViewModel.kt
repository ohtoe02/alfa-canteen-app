package com.example.canteenapp.ui.family

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FamilyViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Тут будет вся семья!"
    }
    val text: LiveData<String> = _text
}