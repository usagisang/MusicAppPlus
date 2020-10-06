package com.gochiusa.musicapp.plus.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

class MyDividerItemDecoration(context: Context, orientation: Int) :
    DividerItemDecoration(context, orientation) {

    private var selfOrientation: Int = orientation

    private val mBounds = Rect()

    override fun setOrientation(orientation: Int) {
        super.setOrientation(orientation)
        selfOrientation = orientation
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null || drawable == null) {
            return
        }
        if (selfOrientation == VERTICAL) {
            drawVertical(c, parent)
        } else {
            super.onDraw(c, parent, state)
        }
    }

    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(
                left, parent.paddingTop, right,
                parent.height - parent.paddingBottom
            )
        } else {
            left = 0
            right = parent.width
        }
        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, mBounds)
            val bottom = mBounds.bottom + Math.round(child.translationY)
            val top = bottom - drawable?.intrinsicHeight!!
            drawable?.setBounds(left, top, right, bottom)
            drawable?.draw(canvas)
        }
        canvas.restore()
    }
}