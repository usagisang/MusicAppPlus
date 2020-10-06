package com.gochiusa.musicapp.plus.tasks.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.tasks.main.child.RecommendPageFragment
import com.gochiusa.musicapp.plus.tasks.main.child.UserPageFragment
import com.gochiusa.musicapp.plus.tasks.main.search.SearchFragment
import com.gochiusa.musicapp.plus.util.FragmentManageUtil
import com.google.android.material.tabs.TabLayout

class MainPageFragment: Fragment() {

    private lateinit var searchButton: Button
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout

    private val firstPageTitle = "首页"
    private val userPageTitle = "用户"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val parent = inflater.inflate(R.layout.fragment_main_page, container, false)
        initChildView(parent)
        initViewPager()
        // 设置搜索按钮的点击事件
        searchButton.setOnClickListener{
            FragmentManageUtil.addFragmentToBackStack(R.id.frame_layout_main_page,
                SearchFragment(), this)
        }
        return parent
    }

    private fun initChildView(parentView: View) {
        searchButton = parentView.findViewById(R.id.btn_main_search)
        tabLayout = parentView.findViewById(R.id.tab_layout)
        viewPager = parentView.findViewById(R.id.vp_main_page_fragment)
    }

    /**
     * 初始化ViewPager和TabLayout的显示
     */
    private fun initViewPager() {
        tabLayout.setupWithViewPager(viewPager, false)
        // 初始化ViewPager的适配器
        viewPager.adapter = object : FragmentStatePagerAdapter(
            FragmentManageUtil.fragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            override fun getItem(position: Int): Fragment {
                return if (position == 0) {
                    RecommendPageFragment()
                } else {
                    UserPageFragment()
                }
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return if (position == 0) {
                    firstPageTitle
                } else {
                    userPageTitle
                }
            }
        }
    }
}