package com.gochiusa.musicapp.plus.tasks.main.child

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gochiusa.musicapp.library.util.ContextProvider
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.adapter.FunctionButtonAdapter
import com.gochiusa.musicapp.plus.base.BaseFragment
import com.gochiusa.musicapp.plus.entity.UpdateInformation
import com.gochiusa.musicapp.plus.util.VersionUtil
import com.gochiusa.musicapp.plus.widget.DefaultDecoration

class RecommendPageFragment: BaseFragment<RecommendContract.Presenter>(),
    RecommendContract.View, FunctionButtonAdapter.OnButtonClickListener {

    private lateinit var buttonRecyclerView: RecyclerView

    private lateinit var playlistRecommendTextView: TextView
    private lateinit var playlistRecommendRecyclerView: RecyclerView

    private lateinit var songRecommendTextView: TextView
    private lateinit var songRecommendRecyclerView: RecyclerView

    private lateinit var buttonAdapter: FunctionButtonAdapter

    private lateinit var alertDialogBuilder: AlertDialog.Builder


    override fun onBindPresenter(): RecommendContract.Presenter {
       return RecommendPagePresenter(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        buttonAdapter = FunctionButtonAdapter(context)
        alertDialogBuilder = AlertDialog.Builder(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val parent = inflater.inflate(R.layout.fragment_main_content, container, false)
        initChildView(parent)
        return parent
    }

    private fun initChildView(parentView: View) {
        buttonRecyclerView = parentView.findViewById(R.id.rv_main_function_button)
        playlistRecommendRecyclerView = parentView.findViewById(R.id.rv_main_playlist_recommend_content)
        playlistRecommendTextView = parentView.findViewById(R.id.tv_main_playlist_recommend_title)
        songRecommendRecyclerView = parentView.findViewById(R.id.rv_main_song_recommend_content)
        songRecommendTextView = parentView.findViewById(R.id.tv_main_song_recommend_title)
        initFunctionButton(buttonRecyclerView)
    }

    /**
     *  初始化功能按钮以及相关子控件
     */
    private fun initFunctionButton(recyclerView: RecyclerView) {
        // 初始化布局管理器
        recyclerView.layoutManager = LinearLayoutManager(context,
            RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = buttonAdapter
        buttonAdapter.onButtonClickListener = this
        recyclerView.addItemDecoration(DefaultDecoration())
    }

    override fun onClick(position: Int) {
        when (position) {
            3 -> {
                presenter.requestUpdate()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            STORE_PERMISSION_UPDATE_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    presenter.downloadUpdate()
                    showToast(DOWNLOAD_START_TIP)
                } else {
                    showToast(REFUSE_STORE_PERMISSION_TIP)
                }
            }
        }
    }

    override fun canUpdate(updateInformation: UpdateInformation) {
        alertDialogBuilder.setTitle(ALERT_TITLE)
        val versionName = VersionUtil.getVersionName()
        alertDialogBuilder.setMessage(
            "发现新版本${updateInformation.versionName}，当前版本为${versionName}\n" +
                    "新版本的特性：\n${updateInformation.versionDescription}")
        alertDialogBuilder.setCancelable(true)
        alertDialogBuilder.setPositiveButton("确认更新") { _: DialogInterface, _: Int ->
            // 如果已经有了存储权限
            if (ContextCompat.checkSelfPermission(ContextProvider.context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                presenter.downloadUpdate()
                showToast(DOWNLOAD_START_TIP)
            } else {
                // 否则尝试申请权限
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORE_PERMISSION_UPDATE_REQUEST_CODE)
            }
        }
        alertDialogBuilder.setNegativeButton("取消") { _: DialogInterface, _: Int -> }
        alertDialogBuilder.show()
    }

    override fun notUpdate() {
        showToast(NOT_UPDATE_TIP)
    }

    companion object {
        private const val ALERT_TITLE = "是否确认更新？"
        private const val DOWNLOAD_START_TIP = "新版本下载中......"
        private const val NOT_UPDATE_TIP = "当前已经是最新版本"
        private const val REFUSE_STORE_PERMISSION_TIP = "拒绝存储权限将无法使用该功能"
        private const val STORE_PERMISSION_UPDATE_REQUEST_CODE = 1
    }
}