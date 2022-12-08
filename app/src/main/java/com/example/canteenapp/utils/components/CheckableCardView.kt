package com.example.canteenapp.utils.components

import android.R
import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import androidx.cardview.widget.CardView


public class CheckableCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr), Checkable {

    private var isChecked = false

    init {
    }

    override fun performClick(): Boolean {
        toggle()
        return super.performClick()
    }

    override fun setChecked(checked: Boolean) {
        this.isChecked = checked
    }

    override fun isChecked(): Boolean {
        return isChecked
    }

    override fun toggle() {
        setChecked(!this.isChecked)
    }

    private val CHECKED_STATE_SET = intArrayOf(
        R.attr.state_checked
    )

    override fun onCreateDrawableState(extraSpace: Int): IntArray? {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET)
        }
        return drawableState
    }
}