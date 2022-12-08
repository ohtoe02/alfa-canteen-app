package com.example.canteenapp.ui.dialogs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddDishDialogViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Диалог!"
    }
    val text: LiveData<String> = _text
}