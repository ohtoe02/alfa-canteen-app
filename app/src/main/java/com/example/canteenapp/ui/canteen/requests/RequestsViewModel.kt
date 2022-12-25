package com.example.canteenapp.ui.canteen.requests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RequestsViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Тут будет заказ"
    }
    val text: LiveData<String> = _text
}