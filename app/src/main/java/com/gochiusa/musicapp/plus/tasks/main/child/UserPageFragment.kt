package com.gochiusa.musicapp.plus.tasks.main.child

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.adapter.UserPlaylistAdapter
import com.gochiusa.musicapp.plus.base.BaseFragment
import com.gochiusa.musicapp.plus.entity.User
import com.gochiusa.musicapp.plus.entity.UserPlaylist
import com.gochiusa.musicapp.plus.widget.DefaultDecoration
import com.gochiusa.musicapp.plus.widget.RoundImageView
import com.squareup.picasso.Picasso

class UserPageFragment: BaseFragment<UserContract.Presenter>(), UserContract.View {

    private lateinit var usernameText: EditText
    private lateinit var passwordText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var loginButton: Button
    private lateinit var roundImageView: RoundImageView
    private lateinit var nickNameTextView: TextView
    private lateinit var logoutButton: Button

    /**
     * 显示用户的所有歌单
     */
    private lateinit var userPlaylistRecyclerView: RecyclerView

    /**
     * 显示固定提示标题的TextView，因为需要控制显示与隐藏，一并绑定上来
     */
    private lateinit var userPlaylistTitle: TextView


    override fun onBindPresenter(): UserContract.Presenter {
        return UserPagePresenter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val parent = inflater.inflate(R.layout.fragment_main_user_and_login, container, false)
        initChildView(parent)
        // 注册按钮的点击事件
        loginButton.setOnClickListener{ loginButtonAction() }
        logoutButton.setOnClickListener{ logoutButtonAction() }
        // 请求登录数据
        presenter.checkLogin()
        return parent
    }


    private fun initChildView(parent: View) {
        usernameText = parent.findViewById(R.id.edt_login_phone_number)
        passwordText = parent.findViewById(R.id.edt_login_password)
        loginButton = parent.findViewById(R.id.btn_main_login_request)
        progressBar = parent.findViewById(R.id.progress_bar_login)
        roundImageView = parent.findViewById(R.id.iv_child_user_avatar)
        nickNameTextView = parent.findViewById(R.id.tv_child_user_nickname)
        logoutButton = parent.findViewById(R.id.btn_child_logout)
        userPlaylistRecyclerView = parent.findViewById(R.id.rv_child_user_total_list)
        userPlaylistTitle = parent.findViewById(R.id.tv_child_user_list_title)
    }

    override fun loginSuccess(user: User) {
        usernameText.visibility = View.GONE
        passwordText.visibility = View.GONE
        loginButton.visibility = View.GONE
        progressBar.visibility = View.GONE
        roundImageView.visibility = View.VISIBLE
        nickNameTextView.visibility = View.VISIBLE
        logoutButton.visibility = View.VISIBLE
        // 显示用户歌单控件
        userPlaylistRecyclerView.visibility = View.VISIBLE
        userPlaylistTitle.visibility = View.VISIBLE

        // 发起加载用户歌单请求
        presenter.requestUserPlaylist(user.userId)

        nickNameTextView.text = user.nickName
        Picasso.get().load(user.avatarUrl).fit().into(roundImageView)
    }

    override fun resetInterface() {
        progressBar.visibility = View.GONE
        usernameText.visibility = View.VISIBLE
        passwordText.visibility = View.VISIBLE
        loginButton.visibility = View.VISIBLE
        roundImageView.visibility = View.GONE
        nickNameTextView.visibility = View.GONE
        logoutButton.visibility = View.GONE
        userPlaylistRecyclerView.visibility = View.GONE
        userPlaylistTitle.visibility = View.GONE

        // 清除可能存在的文本
        usernameText.setText("", TextView.BufferType.EDITABLE)
        passwordText.setText("", TextView.BufferType.EDITABLE)
    }

    override fun showLoading(loading: Boolean) {
        if (loading) {
            progressBar.visibility = View.VISIBLE
            loginButton.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            loginButton.isEnabled = true
        }
    }

    override fun loadUserPlaylistSuccess(userPlaylist: MutableList<UserPlaylist>) {
        // 数据加载成功后，初始化显示数据的列表
        userPlaylistRecyclerView.layoutManager = LinearLayoutManager(context)
        userPlaylistRecyclerView.addItemDecoration(DefaultDecoration(
            10, 10, 10, 10))
        userPlaylistRecyclerView.adapter = UserPlaylistAdapter(userPlaylist)
    }

    private fun loginButtonAction() {
        // 如果某一个输入框为空
        if (EMPTY_STRING.contentEquals(usernameText.text)) {
            usernameText.error = EMPTY_TIP
            return
        }
        if (EMPTY_STRING.contentEquals(passwordText.text)) {
            passwordText.error = EMPTY_TIP
            return
        }
        // 显示加载的进度条
        showLoading(true)
        presenter.loginRequest(usernameText.text.toString(), passwordText.text.toString())
    }

    private fun logoutButtonAction() {
        presenter.logout()
        resetInterface()
    }

    companion object {
        private const val EMPTY_TIP = "输入不能为空"
        private const val EMPTY_STRING = ""
    }
}