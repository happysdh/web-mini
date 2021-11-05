package com.uniondrug.udlib.web.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.bingoogolapple.qrcode.core.QRCodeView
import com.uniondrug.udlib.web.common.Extras
import com.uniondrug.udlib.web.R
import com.uniondrug.udlib.web.utils.AlexStatusBarUtils
import kotlinx.android.synthetic.main.udweb_activity_qr_code.*
import java.io.Serializable

class QRCodeActivity : AppCompatActivity(), QRCodeView.Delegate, View.OnClickListener {
    private lateinit var mMap: MutableMap<String, String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.udweb_activity_qr_code)
        mMap = intent.getSerializableExtra("map") as MutableMap<String, String>
        tvTitle.text = mMap["scanTitle"]
        tvMid.text = mMap["scanMidCon"]
        if (!mMap["scanFootCon"].isNullOrEmpty()) {
            tvBottom.text = mMap["scanFootCon"]
            tvBottom.visibility = View.VISIBLE
        }

        AlexStatusBarUtils.setTransparentStatusBar(this, null)
        zxingview.setDelegate(this)
        imgClose.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        zxingview.startCamera() // 打开后置摄像头开始预览，但是并未开始识别
        //        mZXingView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); // 打开前置摄像头开始预览，但是并未开始识别
        zxingview.startSpotAndShowRect() // 显示扫描框，并开始识别
    }

    override fun onStop() {
        zxingview.stopCamera() // 关闭摄像头预览，并且隐藏扫描框
        super.onStop()
    }

    override fun onDestroy() {
        zxingview.onDestroy() // 销毁二维码扫描控件
        super.onDestroy()
    }

    companion object {
        fun start(activity: Activity, requestCode: Int, map: MutableMap<String, String>) {
            val intent = Intent(activity, QRCodeActivity::class.java)
            intent.putExtra("map", map as Serializable)
            activity.startActivityForResult(intent, requestCode)
        }
    }

    override fun onScanQRCodeSuccess(result: String?) {
        Log.w("TAG", "onScanQRCodeSuccess: $result")
        val intent = Intent()
        intent.putExtra(Extras.EXTRA_FILE_PATH, result)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {
        // 这里是通过修改提示文案来展示环境是否过暗的状态，接入方也可以根据 isDark 的值来实现其他交互效果
    }

    override fun onScanQRCodeOpenCameraError() {

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgClose -> {
                finish()
            }
        }
    }
}