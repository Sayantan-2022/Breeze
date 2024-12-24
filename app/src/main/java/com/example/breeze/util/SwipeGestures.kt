package com.example.breeze.util

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.breeze.R

abstract class SwipeGestures(context : Context) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {
    private val deleteColor = ContextCompat.getColor(context, R.color.red)
    private val deleteIcon = R.drawable.round_delete_24

    private val bookmarkColor = ContextCompat.getColor(context, R.color.green)
    private val bookmarkIcon = R.drawable.baseline_bookmarks_24
}