package com.example.canteenapp.utils.models

import java.io.Serializable

data class DishData(
    var id:String,
    val weight:String,
    val title: String,
    val category: String,
    val price: String,
    val picture: String,
    var isActive: Boolean = false
    ) : Serializable
