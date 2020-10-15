package com.gochiusa.musicapp.plus.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gochiusa.musicapp.plus.R

/**
 * 一个额外包含了尾布局的适配器
 * 继承自`ListAdapter`，使用列表集合来管理数据源
 * @param <T> 需要存放的数据的类型
 * @param <V> ViewHolder的类型，必须继承自`FootViewAdapter.NormalViewHolder`
</V></T> */
abstract class FootViewAdapter<T, V : FootViewAdapter.NormalViewHolder>(list: MutableList<T>):
    ListAdapter<T, FootViewAdapter.NormalViewHolder> (list) {

    /**
     * 尾布局
     */
    protected open lateinit var footView: View

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NormalViewHolder {
        return if (viewType == CONTENT_TYPE) {
            createContentView(parent)
        } else {
            createFootView(parent)
        }
    }

    /**
     * 生成尾布局
     * @return 尾布局的ViewHolder
     */
    protected open fun createFootView(parent: ViewGroup): NormalViewHolder {
        // 如果尾布局的View尚未创建
        if (! this::footView.isInitialized) {
            footView = LayoutInflater.from(parent.context).inflate(
                R.layout.item_list_footer_view, parent, false
            )
        }
        // 默认隐藏尾布局
        footView.visibility = View.GONE
        return FootViewHolder(footView)
    }

    override fun onBindViewHolder(
        holder: NormalViewHolder,
        position: Int
    ) {
        if (holder.type == FOOT_TYPE) {
            onBindFootViewHolder(holder, position)
        } else {
            onBindContentViewHolder(holder, position)
        }
    }

    override fun getItemCount(): Int {
        return countItems() + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == countItems()) {
            FOOT_TYPE
        } else {
            CONTENT_TYPE
        }
    }

    /**
     * 生成携带普通内容的View的ViewHolder，需要子类实现
     */
    abstract fun createContentView(parent: ViewGroup): NormalViewHolder

    /**
     * 需要子类实现，为普通内容的View绑定数据显示的相关操作
     * @param holder 待绑定数据的ViewHolder
     * @param position ViewHolder在列表中的位置
     */
    abstract fun onBindContentViewHolder(
        holder: NormalViewHolder,
        position: Int
    )

    /**
     * 处理尾布局，默认什么也不做
     */
    fun onBindFootViewHolder(
        holder: NormalViewHolder,
        position: Int
    ) {}

    fun hideFootView() {
        footView.visibility = View.GONE
    }

    fun showFootView() {
        footView.visibility = View.VISIBLE
    }

    /**
     * 尾布局和普通布局的父类
     */
    open class NormalViewHolder(itemView: View,
        val type: Int): RecyclerView.ViewHolder(itemView)

    /**
     * 尾布局的ViewHolder
     */
    protected class FootViewHolder(footView: View): NormalViewHolder(footView, FOOT_TYPE)

    companion object {
        /**
         * 子View的类型为内容类型
         */
        var CONTENT_TYPE = 0

        /**
         * 子View的类型为尾布局类型
         */
        var FOOT_TYPE = 1
    }
}