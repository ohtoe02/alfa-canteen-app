package com.example.canteenapp.utils.models

data class MyRequestData(
    val id: String,
    val dishes: HashMap<String, Pair<String?, String?>>,
    val info: HashMap<String, String>
)
