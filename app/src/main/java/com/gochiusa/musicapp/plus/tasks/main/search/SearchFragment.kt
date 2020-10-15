package com.gochiusa.musicapp.plus.tasks.main.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.gochiusa.musicapp.library.util.ContextProvider
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.adapter.SearchSongAdapter
import com.gochiusa.musicapp.plus.base.BaseFragment
import com.gochiusa.musicapp.plus.entity.Song
import com.gochiusa.musicapp.plus.util.FragmentManageUtil
import com.gochiusa.musicapp.plus.widget.MyDividerItemDecoration

class SearchFragment: BaseFragment<SearchContract.Presenter>(), SearchContract.View {

    private lateinit var backButton: Button
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    /**
     *  标志变量，为false时不再提交加载更多的请求
     */
    private var hasMore = true

    /**
     *  显示数据的适配器
     */
    private val adapter: SearchSongAdapter = SearchSongAdapter(mutableListOf())

    override fun onBindPresenter(): SearchContract.Presenter = SearchPresenter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val parent = inflater.inflate(R.layout.fragment_search, container, false)
        initChildView(parent)
        // 请求默认搜索关键字
        presenter.getDefaultKeyword()
        return parent
    }

    override fun setDefaultKeyword(defaultKeyword: String) {
        searchView.queryHint = defaultKeyword
    }

    override fun searchCallback(songList: List<Song>) {
        adapter.addAll(songList)
        this.hasMore = true
        hideRefreshView()

    }

    override fun searchFailure(hasMore: Boolean) {
        this.hasMore = hasMore
        hideRefreshView()
    }

    override fun removeAllSongs() {
        adapter.clear()
    }

    private fun initChildView(parent: View) {
        backButton = parent.findViewById(R.id.btn_search_back)
        searchView = parent.findViewById(R.id.search_view)
        recyclerView = parent.findViewById(R.id.rv_content)
        swipeRefreshLayout = parent.findViewById(R.id.swipe_refresh)

        // 设置返回按钮的点击事件
        backButton.setOnClickListener {
            // 关闭软键盘
            searchView.clearFocus()
            FragmentManageUtil.fragmentManager.popBackStack()
        }
        // 设置SwipeRefreshLayout的刷新事件
        swipeRefreshLayout.setOnRefreshListener {
            presenter.refresh()
        }
        initRecyclerView(recyclerView)
        initSearchView(searchView)
    }
    /**
     *  初始化RecyclerView
     */
    private fun initRecyclerView(recyclerView: RecyclerView) {
        // 添加分隔线
        recyclerView.addItemDecoration(MyDividerItemDecoration(ContextProvider.context,
            DividerItemDecoration.VERTICAL))
        // 设置RecyclerView为线性布局
        recyclerView.layoutManager = LinearLayoutManager(ContextProvider.context)
        // 设置适配器
        recyclerView.adapter = this.adapter
        // 为recyclerView添加滚动的监听器
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // 当还能加载更多、不处于刷新状态、不能再向下滑动时，允许加载更多
                val requestMore = hasMore && (! swipeRefreshLayout.isRefreshing) &&
                        (! recyclerView.canScrollVertically(1))
                if (requestMore) {
                    // 显示尾布局
                    adapter.showFootView()
                    // 提交加载更多的操作
                    presenter.showMore()
                }
            }
        })
    }
    /**
     *  初始化SearchView的设置
     */
    private fun initSearchView(searchView: SearchView) {
        searchView.onActionViewExpanded()
        // 设置提交的监听器
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // 去除前后空格
                val keyWord = query.trim { it <= ' ' }
                // 关闭软键盘
                searchView.clearFocus()
                // 提交搜索
                if (keyWord.isEmpty()) {
                    presenter.submitNewSearch(searchView.queryHint.toString())
                } else {
                    presenter.submitNewSearch(query)
                }
                // 显示刷新控件
                swipeRefreshLayout.isRefreshing = true
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean = false
        })
    }

    /**
     * 隐藏与刷新相关的控件
     */
    private fun hideRefreshView() {
        swipeRefreshLayout.isRefreshing = false
        adapter.hideFootView()
    }
}