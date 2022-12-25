package com.example.canteenapp.ui.canteen.all_dishes_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AllDishesListViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Тут будет заказ"
    }
    val text: LiveData<String> = _text
}