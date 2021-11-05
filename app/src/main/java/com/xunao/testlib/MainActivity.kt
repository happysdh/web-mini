package com.xunao.testlib

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkRequest
import com.uniondrug.udlib.web.UDWebCallBack
import com.uniondrug.udlib.web.UDWebSDK
import com.uniondrug.udlib.web.activity.UDWebFragment
import com.uniondrug.udlib.web.utils.ToastUtils
import com.xunao.testlib.andserv.ServerManager
import com.xunao.testlib.andserv.ServerManager.OnServeBack
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private var mServerManager: ServerManager? = null
    private var hostUrl = ""
    private lateinit var tvWebServer: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        UDWebSDK.getInstance().init(2)
        // AndServer run in the service.
        mServerManager = ServerManager(this, object : OnServeBack {
            override fun onServerStart(ip: String) {
                Log.w("TAG", "onServerStart: $ip")
                hostUrl = "http://$ip:8080/"
                tvWebServer.text = "Web服务开启：" + hostUrl
            }

            override fun onServerError(msg: String) {
                Log.w("TAG", "onServerError: " + msg)
            }

            override fun onServerStop() {}
        })
//        UDWebFragment()
        mServerManager?.register()
        val uploadWorkRequest: WorkRequest =
            PeriodicWorkRequestBuilder<CoroutineBlueToothWorker>(
                15, TimeUnit.MINUTES
            )
//            OneTimeWorkRequestBuilder<CoroutineBlueToothWorker>()
                .build()
        findViewById<TextView>(R.id.tvBrowser).setOnClickListener {
            if (!TextUtils.isEmpty(hostUrl)) {
                val intent = Intent()
                intent.action = "android.intent.action.VIEW"
                intent.data = Uri.parse(hostUrl)
                startActivity(intent)
            }
        }

        val btn = findViewById<TextView>(R.id.tvBtn)
        tvWebServer = findViewById<TextView>(R.id.tvWebServer)
        tvWebServer.setOnClickListener {
            mServerManager?.startServer()
        }
        findViewById<TextView>(R.id.tvWebOpen).setOnClickListener {
            if (!TextUtils.isEmpty(hostUrl)) {
                val map: MutableMap<String, String?> = HashMap()
                map["token"] = "d2271bdc-57a0-415d-9e0a-188913c4f659"
                map["assistantId"] = "292985"
//            UDWebSDK.getInstance().startApp(this,"business")
                UDWebSDK.getInstance().init(3)
//            UDWebSDK.getInstance().startApp(
//                this,
//                "http://yaodianbao.turboradio.cn/yaodianbao/test",
//                map
//            )
//                UDWebSDK.getInstance().startApp(
//                    this,
//                    "http://www.hello.com/" + "im_V1.0.1/#/chat/p2p-93052408141f4605a1dbd6cb1744m?uid=fec626b6f8c24443a8948596cedam&sdktoken=fec626b6f8c24443a8948596cedac61c&type=2&history=1&loginType=ydbapp&timeTag=1626411568&assistantId=292985&_env=3&_t=1626413446106&token=d2271bdc-57a0-415d-9e0a-188913c4f659",
//                    map
//                )
            }
        }

        findViewById<TextView>(R.id.tvProxyWebOpen).setOnClickListener {
            if (!TextUtils.isEmpty(hostUrl)) {
                val map: MutableMap<String, String?> = HashMap()
                map["token"] = "d2271bdc-57a0-415d-9e0a-188913c4f659"
                map["assistantId"] = "292985"
//            UDWebSDK.getInstance().startApp(this,"business")
                UDWebSDK.getInstance().init(3)
//            UDWebSDK.getInstance().startApp(
//                this,
//                "https://www.baidu.com",
//                map
//            )
                UDWebSDK.getInstance().startApp(
                    this,
                    "http://udshop.uniondrug.com/im_V1.0.1/#/chat/p2p-93052408141f4605a1dbd6cb1744m?uid=fec626b6f8c24443a8948596cedam&sdktoken=fec626b6f8c24443a8948596cedac61c&type=2&history=1&loginType=ydbapp&timeTag=1626411568&assistantId=292985&_env=3&_t=1626413446106&token=d2271bdc-57a0-415d-9e0a-188913c4f659",
                    map
                )
            }
        }

        findViewById<TextView>(R.id.tvBtnFragment).setOnClickListener {
            startActivity(Intent(this, TestFragmentActivity::class.java))
        }

        btn.setOnClickListener {
            val map: MutableMap<String, String?> = HashMap()
            map["token"] = "ce425e51-2f59-4258-b8d1-d0f7d28bda74"
//            map["assistantId"] = "292985"
//            UDWebSDK.getInstance().startApp(this,"business")
            UDWebSDK.getInstance().init(1)
//            UDWebSDK.getInstance().startApp(
//                this,
//                "https://www.baidu.com",
//                map
//            )
//            UDWebSDK.getInstance().startApp(
//                this,
////                "uniondrugshop://app/udweb?mpProject=take",
////                "$hostUrl" + "#/yaodianbao/openClass/home?token=1bf17cbb-1fbb-44d0-82a3-3171d19252ea",
//                "uniondrugshop://app/udweb?mpProject=education",
////                "uniondrugshop://app/udweb?mpProject=education&mpQuery=courseId%3D274",
////                "uniondrugshop://app/udweb?mpProject=medicine",
////                "uniondrugshop://app/udweb?mpProject=im&mpUrl=%2Fchat%2Fp2p-8de694298bd44338a4964fed894ct&mpQuery=uid%3Dad03b56755ec48aaa8b5aeb68f82t%26sdktoken%3Dad03b56755ec48aaa8b5aeb68f82438c%26type%3D2%26history%3D1%26loginType%3Dydbapp%26timeTag%3D1624873746",
////                "uniondrugshop://app/udweb?mpProject=education&mpUrl=%2Fyaodianbao%2FopenClass%2Fdetail&mpQuery=courseId%3D274",
////                "uniondrugshop://app/udweb?mpProject=education&mpRouter=%2Fyaodianbao%2FopenClass%2Fhome&mpQuery=courseId%3D274",
//                map
//            )

            UDWebSDK.getInstance().startApp(
                this,
//                "file:///android_asset/wkwebview.html",
                "http://yaodianbao.turboradio.cn/yaodianbao/test",
                map
            )
            UDWebSDK.getInstance().setMPEventCallBack(object : UDWebCallBack {
                override fun onCallBack(event: String, data: String) {
                    Log.w("jsPlatform", "callback:event:$event,data$data")
                    when (event) {
                        //用户信息
                        "UDuserInfo" -> {
                            UDWebSDK.getInstance().sendMPEvent(
                                event,
                                "{\"data\":{\"data\":{\"key\":\"value\"}},\"msg\":\"成功\",\"status\":\"1\"}"
                            )
                        }
                        //版本
                        "UDAppVersion" -> {
                            UDWebSDK.getInstance().sendMPEvent(
                                event,
                                "{\"data\":{\"data\":\"4.7.0\"},\"msg\":\"成功\",\"status\":\"1\"}"
                            )
                        }
                        //回到首页
                        "UDbackToHome" -> {

                        }
                        //分享
                        "UDserviceShare" -> {

                        }
                        //截屏后反馈
                        "UDtoFeedBack" -> {

                        }
                        //积分反馈
                        "UDpointsFeedback" -> {

                        }
                        //回到会员首页
                        "UDbackToMemberRoot" -> {

                        }
                        //商保新会员，进入用户详情
                        "UDtoMember" -> {

                        }
                        else -> {
                            ToastUtils.showShortToast(this@MainActivity, "$event 未实现")
                        }
                    }
                }
            })
        }

//        logStatus()
    }
//
//    var logRun: Runnable = Runnable {
//        val mKeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
//        val flag = mKeyguardManager.inKeyguardRestrictedInputMode()
//        Log.w("TAGFLAG", ": $flag")
//        logStatus()
//    }
//
//    private fun logStatus() {
//        Handler().postDelayed(logRun,3000)
//    }
}