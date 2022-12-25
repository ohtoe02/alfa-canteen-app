package com.example.canteenapp.ui.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OrderViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Тут будет заказ"
    }
    val text: LiveData<String> = _text
}