package com.uniondrug.udlib.web

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.uniondrug.udlib.web.activity.UDWebViewActivity
import com.uniondrug.udlib.web.common.EventBusMessage
import com.uniondrug.udlib.web.utils.StorageUtils
import com.uniondrug.udlib.web.utils.ToastUtils
import org.greenrobot.eventbus.EventBus
import java.io.Serializable

class UDWebSDK {
    private var callBackFunction: UDWebCallBack? = null

    //"1","2","3"
    var environment: Int = 0

    companion object {
        private var instance: UDWebSDK? = null

        @JvmStatic
        fun getInstance(): UDWebSDK {
            if (instance == null) {
                instance = UDWebSDK()
            }
            return instance!!
        }
    }

    fun init(env: Int) {
        if (env <= 0 || env > 3) environment = 1
        environment = env
    }

    fun startApp(context: Context, link: String) {
        val temp: MutableMap<String, String?> = HashMap()
        startApp(context, link, temp)
    }

    fun startApp(
        context: Context,
        link: String,
        map: MutableMap<String, String?>
    ) {


        if (environment <= 0 || environment > 3) {
            ToastUtils.showLongToast(context, "未初始化SDK")
        } else {
            StorageUtils.init(context, "")
            try {
                val uri = Uri.parse(link)
                if (uri.scheme != "http" && uri.scheme != "https" && uri.scheme != "uniondrugshop" && uri.scheme!="file") {
                    ToastUtils.showLongToast(context, "入参错误")
                    return
                }
                for (key in uri.queryParameterNames) {
                    map[key] = uri.getQueryParameter(key)
                }
                val intent = Intent(context, UDWebViewActivity::class.java)
                intent.putExtra("link", link)
                intent.putExtra("mapData", map as Serializable)
                context.startActivity(intent)
            } catch (e: Exception) {
                ToastUtils.showLongToast(context, e.toString())
            }
        }
    }

    fun finishApp() {
        EventBus.getDefault().post(EventBusMessage(EventBusMessage.MSG_TYPE_APP_FINISH, ""))
    }

    fun refreshApp() {
        EventBus.getDefault().post(EventBusMessage(EventBusMessage.MSG_TYPE_APP_REFRESH, ""))
    }

    fun reloadApp(){
        EventBus.getDefault().post(EventBusMessage(EventBusMessage.MSG_TYPE_APP_RELOAD_DATA, ""))
    }

    fun sendMPEvent(event: String, data: String) {
        EventBus.getDefault().post(EventBusMessage(EventBusMessage.MSG_TYPE_APP_TO_MP, event, data))
    }

    fun setMPEventCallBack(callback: UDWebCallBack) {
        callBackFunction = callback
    }

    fun getMPEventCallBack(): UDWebCallBack? {
        return callBackFunction
    }
}