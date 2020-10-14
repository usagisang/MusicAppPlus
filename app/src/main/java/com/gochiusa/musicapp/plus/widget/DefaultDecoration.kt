package com.gochiusa.musicapp.plus.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class DefaultDecoration(private val leftPadding: Int, private val topPadding: Int,
                        private val rightPadding: Int, private val bottomPadding: Int) : ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(leftPadding, topPadding, rightPadding, bottomPadding)
    }
}