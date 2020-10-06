package com.gochiusa.musicapp.plus.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

/**
 * 封装了对List数据源的基本操作的一个适配器类
 * @param <T> 数据类型
 * @param <V> ViewHolder的类型
 */
abstract class ListAdapter<T, V : ViewHolder?> (private val itemList: MutableList<T>):
    RecyclerView.Adapter<V>() {

    /**
     * 线程锁
     */
    private val lock = Any()
    fun clear() {
        synchronized(lock) {
            itemList.clear()
            notifyDataSetChanged()
        }
    }

    fun addAll(newList: List<T>) {
        synchronized(lock) {
            val originSize = itemCount
            if (itemList.addAll(newList)) {
                // 如果addAll添加成功，通知刷新
                // （在ArrayList中，其实是做了一个加入的Collection长度是否为0的判断）
                notifyItemRangeInserted(originSize, newList.size)
            }
        }
    }

    fun add(item: T) {
        synchronized(lock) {
            itemList.add(item)
            // 通知View，最新的项目插入到列表最后一位
            notifyItemInserted(itemCount - 1)
        }
    }

    fun remove(item: T) {
        synchronized(lock) {
            // 获取移除的位置
            val position = getPosition(item)
            if (itemList.remove(item)) {
                // 如果移除成功，则先通知子项已经被删除
                notifyItemRemoved(position)
                // 然后从被移除的位置开始通知，刷新后续子项的位置position，使之不错位
                notifyItemRangeChanged(position, countItems() - position)
            }
        }
    }

    fun getReadOnlyList(): List<T> {
        return itemList
    }

    fun getItem(position: Int): T {
        return itemList[position]
    }

    fun getPosition(item: T): Int {
        return itemList.indexOf(item)
    }

    /**
     * 获取列表里所有元素的个数
     */
    fun countItems(): Int {
        return itemList.size
    }

    override fun getItemCount(): Int {
        return countItems()
    }

}