package com.uniondrug.udlib.web.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.ImageView
import android.widget.LinearLayout
import com.github.lzyzsd.jsbridge.BridgeHandler
import com.github.lzyzsd.jsbridge.BridgeWebViewClient
import com.github.lzyzsd.jsbridge.DefaultHandler
import com.uniondrug.udlib.web.UDWebSDK
import com.uniondrug.udlib.web.common.JSConst
import com.uniondrug.udlib.web.utils.UDSDKUtils.saveImage
import com.uniondrug.udlib.web.widget.CustomWebView

interface WebSdk {
    class WebParam constructor(
        webview: CustomWebView,
        mpImgBack: ImageView?,
        llError: LinearLayout?
    ) {
        var mpImgBack: ImageView? = null
        var webview: CustomWebView
        var llError: LinearLayout? = null

        init {
            this.mpImgBack = mpImgBack
            this.webview = webview
            this.llError = llError
        }
    }

    fun initWeb(activity: Activity, webParam: WebParam, handler: BridgeHandler) {
        val webview = webParam.webview
        webview.removeJavascriptInterface("searchBoxJavaBridge_")
        webview.removeJavascriptInterface("accessibility")
        webview.removeJavascriptInterface("accessibilityTraversal")
//        webview.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
//            downloadFile(url)
//        }
        val ws = webview.settings
        ws.allowFileAccess = true
        ws.allowFileAccessFromFileURLs = true
        ws.allowUniversalAccessFromFileURLs = true

        // 网页内容的宽度是否可大于WebView控件的宽度
        ws.loadWithOverviewMode = false
        // 保存表单数据
//        ws.saveFormData = true
//        ws.savePassword = false
        // 是否应该支持使用其屏幕缩放控件和手势缩放
        ws.setSupportZoom(true)
        ws.builtInZoomControls = true
        ws.displayZoomControls = false
        // 启动应用缓存
        ws.setAppCacheEnabled(true)
        // 设置缓存模式
        ws.cacheMode = WebSettings.LOAD_NO_CACHE
        // setDefaultZoom  api19被弃用
        // 设置此属性，可任意比例缩放。
        ws.useWideViewPort = true
        //        ws.setAllowContentAccess(true);
//        // 缩放比例 1
//        bindingView.webview.setInitialScale(1);
        // 告诉WebView启用JavaScript执行。默认的是false。
        ws.javaScriptEnabled = true
        //  页面加载好以后，再放开图片
        ws.blockNetworkImage = false
        // 使用localStorage则必须打开
        ws.domStorageEnabled = true
        // 排版适应屏幕
        ws.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        // WebView是否支持多个窗口。
        ws.setSupportMultipleWindows(false)
        val ua = ws.userAgentString
        ws.userAgentString = "$ua;navigator.userAgent=native_ydb_mp"
        // webview从5.0开始默认不允许混合模式,https中不能加载http资源,需要设置开启。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ws.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        //设置字体默认缩放大小(改变网页字体大小,setTextSize  api14被弃用)
        ws.textZoom = 100
        ws.javaScriptCanOpenWindowsAutomatically = true
        webview.webViewClient = object : BridgeWebViewClient(webview) {
//            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
//                return super.shouldInterceptRequest(view, request)
//            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                // 如下方案可在非微信内部WebView的H5页面中调出微信支付
                if (url.startsWith("weixin://wap/pay?")) {
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.data = Uri.parse(url)
                    activity.startActivity(intent)
                    return true
                } else if (url.startsWith("uniondrugshop://app")) {
                    try {
                        val callBack = UDWebSDK.getInstance().getMPEventCallBack()
                        callBack?.onCallBack("innerLink", url)
                        return true
                    } catch (e: java.lang.Exception) {
                        Log.w("WEBSDK", "shouldOverrideUrlLoading: " + e.message)
                    }
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.w("WEBSDK", "" + webview.canGoBack() + "onCreateWindow: onPageStarted->$url")

            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                webParam.mpImgBack?.visibility =
                    if (webview.canGoBack()) View.VISIBLE else View.GONE
                Log.w("WEBSDK", "onCreateWindow: onPageFinished$url")
            }

            override fun onLoadResource(view: WebView, url: String) {
                super.onLoadResource(view, url)
                Log.w("WEBSDK", "onCreateWindow: onPageStarted<-$url")
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                Log.w("network:", "onReceivedError: $failingUrl")
                webParam.llError?.visibility = View.VISIBLE
                webview.visibility = View.GONE
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequest,
                errorResponse: WebResourceResponse
            ) {
                Log.w("network:", "onReceivedError:HttpError ")
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.w("WEBSDK", "onReceivedError: " + error.errorCode + error.description)
                    Log.w("WEBSDK", "onReceivedError: " + request.url.toString())
                }
            }
        }

        webview.setOnLongClickListener {
            val hitTestResult = webview.hitTestResult
            if (hitTestResult != null) {
                if (hitTestResult.type == WebView.HitTestResult.IMAGE_TYPE || hitTestResult.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    val pic = hitTestResult.extra //获取图片
                    Thread { saveImage(activity, pic) }.start()
                }
            }
            true
        }
        // 与js交互
        webview.setDefaultHandler(DefaultHandler())
        webview.registerHandler(JSConst.JSCONST_UDUPIMAGE, handler)
        webview.registerHandler(JSConst.JSCONST_UDTEL, handler)
        webview.registerHandler(JSConst.JSCONST_UDSCAN, handler)
        webview.registerHandler(JSConst.JSCONST_UDUSERINFO, handler)
        webview.registerHandler(JSConst.JSCONST_UDVERSION, handler)
        webview.registerHandler(JSConst.JSCONST_UDBACKTOHOME, handler)
        webview.registerHandler(JSConst.JSCONST_UDDOWNIMAGE, handler)
        webview.registerHandler(JSConst.JSCONST_UDTONAVIGATE, handler)
        webview.registerHandler(JSConst.JSCONST_UDCOPY, handler)
        webview.registerHandler(JSConst.JSCONST_UDWXSHARE, handler)
        webview.registerHandler(JSConst.JSCONST_UDTOFEEDBACK, handler)
        webview.registerHandler(JSConst.JSCONST_UDSETHEADBARHIDDEN, handler)
        webview.registerHandler(JSConst.JSCONST_UDSETSHAREHIDDEN, handler)
        webview.registerHandler(JSConst.JSCONST_UDSERVICESHARE, handler)
        webview.registerHandler(JSConst.JSCONST_UDFINISH, handler)
        webview.registerHandler(JSConst.JSCONST_UDDTPLIST, handler)
        webview.registerHandler(JSConst.JSCONST_UDPOINTSFEEDBACK, handler)
        webview.registerHandler(JSConst.JSCONST_BACK_TO_MEMBER_ROOT, handler)
        webview.registerHandler(JSConst.JSCONST_TO_MEMBER, handler)
        webview.registerHandler(JSConst.JSCONST_UD_OPEN_LIVE, handler)
        webview.registerHandler(JSConst.JSCONST_UD_WATCH_LIVE, handler)
        webview.registerHandler(JSConst.JSCONST_UD_UPLOAD_FILE, handler)
        webview.registerHandler(JSConst.JSCONST_UD_AUTH, handler)
        webview.registerHandler(JSConst.JSCONST_UD_DOWNLOAD_FILE, handler)
        webview.registerHandler(JSConst.JSCONST_UD_MEDICINE_AUTH, handler)
        webview.registerHandler(JSConst.JSCONST_UD_SHOW_SHARE_WX, handler)
        webview.registerHandler(JSConst.JSCONST_UD_HIDE_SHARE_WX, handler)
        webview.registerHandler(JSConst.JSCONST_UD_PAY, handler)
        webview.registerHandler(JSConst.JSCONST_UD_WATCH_AGORA, handler)
        webview.registerHandler(JSConst.JSCONST_UDMPSHARE, handler)
        webview.registerHandler(JSConst.JSCONST_UDAPPTOJS, handler)
        webview.registerHandler(JSConst.JSCONST_UDJSTOAPP, handler)
    }

}