package com.mantis.notification

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    // 底部弹窗，用来提供用户选择使用不同功能
    private var dialog : Dialog? = null
    // 弹窗的高度
    private val dialogHeight = 20

    companion object {
        /**
         * 请求权限code
         */
        const val PERMISSION_REQUEST_CODE = 1

        // 通知栏文字颜色
        var sNotificationTitleColor: Int = Integer.MIN_VALUE
        // 通知栏content 文案，用于遍历查找content textView
        const val sNotificationContent = "NOTIFICATION.CONTENT"
        // 通知栏title 文案，用于遍历查找title textView
        const val sNotificationTitle = "NOTIFICATION.TITLE"
        // 通知栏content color
        var sNotificationContentColor: Int = 0
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(PinnedHeaderListView(this))
        requestPermission()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            // 是否可重复通知栏
            R.id.action_duplicate_notification -> {
                showCanDuplicateNotification()
                return true
            }
            // 设置remoteView通知高度
            R.id.action_remote_view_height -> {
                setRemoteViewHeight()
                return true
            }
            // 获取系统默认通知文案颜色
            R.id.action_sys_notification_text_color -> {
                showSysNotificationTextColor()
                return true
            }
            else ->{
                return super.onOptionsItemSelected(item)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            // 如果没有权限，则请求权限
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE)
        } else {
            // 如果已经有权限，则执行相应操作
            // 发送通知等
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授予了 POST_NOTIFICATIONS 权限，可以执行相应操作
                // 发送通知等
                setResult(RESULT_OK)
            } else {
                // 用户拒绝了权限请求，可以提示用户或执行其他逻辑
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    /**
     * 展示 是否允许通知栏上显示多个通知的dialog
     */
    private fun showCanDuplicateNotification() {
        dialog = Dialog(this, R.style.ActionSheetDialogStyle)
        val inflater = LayoutInflater.from(this).inflate(R.layout.repeated_dialog_layout, null)
        inflater.findViewById<TextView>(R.id.repeated_notification_text).setOnClickListener{
            Util.canCreateDuplicateNotification = true
            Toast.makeText(applicationContext, "已可同时弹出多个通知", Toast.LENGTH_LONG).show()
            cancelDialog()
        }
        inflater.findViewById<TextView>(R.id.no_repeated_notification_text).setOnClickListener {
            Util.canCreateDuplicateNotification = false
            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancelAll()
            Toast.makeText(applicationContext, "已禁止同时弹出多个通知", Toast.LENGTH_LONG).show()
            cancelDialog()
        }
        dialog?.apply {
            setContentView(inflater)
            window?.apply {
                setGravity(Gravity.BOTTOM)
                attributes.y = dialogHeight
                show()
            }
        }
    }

    /**
     * 弹出设置remoteView自定义高度的dialog
     */
    private fun setRemoteViewHeight() {
        dialog = Dialog(this, R.style.ActionSheetDialogStyle)
        // 填充对话框的布局
        val inflate = LayoutInflater.from(this).inflate(R.layout.remote_view_height_control_dialog_layout, null)
        // 初始化控件
        inflate.findViewById<TextView>(R.id.high_remote_view_text).setOnClickListener {
            Util.notificationRemoteViewHeight = Util.REMOTEVIEW_HIGH
            Toast.makeText(applicationContext, "已设置通知栏RemoteView高度为高", Toast.LENGTH_LONG).show()
            cancelDialog()
        }
        inflate.findViewById<TextView>(R.id.middle_remote_view_text).setOnClickListener {
            Util.notificationRemoteViewHeight = Util.REMOTEVIEW_MIDDLE
            Toast.makeText(applicationContext, "已设置通知栏RemoteView高度为中", Toast.LENGTH_LONG).show()
            cancelDialog()
        }
        inflate.findViewById<TextView>(R.id.short_remote_view_text).setOnClickListener {
            Util.notificationRemoteViewHeight = Util.REMOTEVIEW_SHOT
            Toast.makeText(applicationContext, "已设置通知栏RemoteView高度为矮", Toast.LENGTH_LONG).show()
            cancelDialog()
        }
        inflate.findViewById<TextView>(R.id.default_remote_view_text).setOnClickListener {
            Util.notificationRemoteViewHeight = Util.REMOTEVIEW_DEFAULT
            Toast.makeText(applicationContext, "已设置通知栏RemoteView高度为默认高度", Toast.LENGTH_LONG).show()
            cancelDialog()
        }
        dialog?.apply {
            setContentView(inflate)
            window?.apply {
                setGravity(Gravity.BOTTOM)
                attributes.y = dialogHeight
                show()
            }
        }
    }

    /**
     * 取消dialog的显示
     */
    private fun cancelDialog() {
        dialog?.apply {
            if (isShowing) cancel()
        }
    }

    /**
     * 展示系统通知栏颜色
     */
    private fun showSysNotificationTextColor() {
        dialog = Dialog(this, R.style.ActionSheetDialogStyle)
        val inflateView = LayoutInflater.from(this).inflate(R.layout.get_notification_text_color_dialog_layout, null)
        Util.getSysNotificationTextColor(applicationContext)
        inflateView.findViewById<TextView>(R.id.sys_notification_text_color).text =
            getString(R.string.sys_notification_text_color_is, sNotificationTitleColor)
        dialog?.apply {
            // 将布局设置给Dialog
            setContentView(inflateView)
            // 获取当前Activity所在的窗体
            window?.apply {
                setGravity(Gravity.BOTTOM)
                //设置Dialog距离底部的距离
                attributes.y = dialogHeight
                show()
            }
        }
    }

}
