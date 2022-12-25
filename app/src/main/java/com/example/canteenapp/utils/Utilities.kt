package com.example.canteenapp.utils

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.google.android.material.snackbar.Snackbar

fun createSnack(view: View, text: String, length: Int, bgColor: Int, textColor: Int) {
    val snack = Snackbar
        .make(view, text, length)
        .setBackgroundTint(bgColor)
        .setTextColor(textColor)

    snack.show()
}

fun createSuccessfulSnack(view: View, message: String) {
    createSnack(
        view, message,
        Snackbar.LENGTH_SHORT,
        Color.parseColor("#D8FFDC"),
        Color.parseColor("#52FF4F")
    )
}

fun createErrorSnack(view: View, message: String) {
    createSnack(
        view, message,
        Snackbar.LENGTH_LONG,
        Color.parseColor("#FFE2E2"),
        Color.parseColor("#FF5D5D")
    )
}

fun betterNavigate(navController: NavController, navigateToId: Int, bundle: Bundle? = null) {
    navController.navigate(
        navigateToId, bundle,
        NavOptions.Builder().setPopUpTo(navController.graph.startDestinationId, false)
            .build()
    )
}
