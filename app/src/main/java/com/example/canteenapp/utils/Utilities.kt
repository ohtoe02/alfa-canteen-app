package com.example.canteenapp.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun createSnack(view: View, text: String, length: Int, bgColor: Int, textColor: Int) {
    val snack = Snackbar
        .make(view, text, length)
        .setBackgroundTint(bgColor)
        .setTextColor(textColor)

    snack.show()
}
