package com.gochiusa.musicapp.plus.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * 为了能够让播放界面下方的操作控件能够自适应屏幕，而自定义的ViewGroup
 */
class MusicControlView(context: Context, attributeSet: AttributeSet?,
                       defStyleAttr: Int, defStyleRes: Int):
    ViewGroup(context, attributeSet, defStyleAttr, defStyleRes) {

    var adapter: Adapter = DefaultAdapter()
        set(value) {
            removeAllViews()
            field = value
            for (i in 0 until value.countItems()) {
                // 子View添加到ViewGroup
                this.addView(createView(i, value))
            }
        }

    /**
     *  子项之间的间距
     */
    private val itemMargin: Int = 20

    constructor(context: Context): this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?):
            this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int):
            this(context, attributeSet, defStyleAttr, 0)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val viewGroupWidth = MeasureSpec.getSize(widthMeasureSpec)
        // 根据控件宽度，计算分配子View的宽度
        val eachViewWidth = (viewGroupWidth -
                itemMargin * (adapter.countItems() + 1)) / adapter.countItems()

        val childMeasureSpec = MeasureSpec.makeMeasureSpec(eachViewWidth, MeasureSpec.AT_MOST)
        var childView: View
        // 依照给定的测量规格，测量每一个子view
        for (index in 0 until childCount) {
            childView = getChildAt(index)
            childView.layoutParams
            // 如果为GONE状态，直接无视
            if (childView.visibility == View.GONE) {
                continue
            }
            measureChild(childView, childMeasureSpec, childMeasureSpec)
        }
        setMeasuredDimension(viewGroupWidth, eachViewWidth)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 根据控件宽度，计算分配子View的宽度
        val eachViewWidth = (width - itemMargin * (adapter.countItems() + 1)) / adapter.countItems()
        var childView: View

        // 当前的定位坐标，其他子View的定位会基于这个坐标进行调整
        var paintLeft = itemMargin

        // 依照给定的测量规格，测量每一个子view
        for (index in 0 until childCount) {
            childView = getChildAt(index)
            // 如果为GONE状态，直接无视
            if (childView.visibility == View.GONE) {
                continue
            }
            // 根据参数定位子View
            childView.layout(
                paintLeft, t, paintLeft + eachViewWidth, t + childView.measuredHeight
            )
            paintLeft += (eachViewWidth + itemMargin)
        }

    }


    private fun createView(position: Int, adapter: Adapter): View {
        // 创建ViewHolder
        val viewHolder: ViewHolder = adapter.onCreateViewHolder(
            this, adapter.getItemViewType(position))
        // 绑定数据
        adapter.onBindViewHolder(viewHolder, position)
        return viewHolder.itemView
    }


    abstract class Adapter {
        abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        abstract fun onBindViewHolder(holder: ViewHolder, position: Int)
        abstract fun countItems(): Int
        fun getItemViewType(position: Int): Int {
            return 0
        }
    }

    abstract class ViewHolder(val itemView: View)

    class DefaultAdapter: Adapter() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return DefaultViewHolder(parent)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {}
        override fun countItems(): Int {return 0}
    }
    class DefaultViewHolder(view: View): ViewHolder(view)
}