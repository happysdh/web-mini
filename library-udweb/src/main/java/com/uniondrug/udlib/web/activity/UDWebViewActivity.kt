package com.uniondrug.udlib.web.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.os.Environment.DIRECTORY_DCIM
import android.os.Environment.getExternalStorageDirectory
import android.text.TextUtils
import android.util.Base64.DEFAULT
import android.util.Base64.decode
import android.util.Log
import android.view.Gravity
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.github.lzyzsd.jsbridge.BridgeHandler
import com.github.lzyzsd.jsbridge.BridgeWebViewClient
import com.github.lzyzsd.jsbridge.CallBackFunction
import com.github.lzyzsd.jsbridge.DefaultHandler
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadSampleListener
import com.liulishuo.filedownloader.FileDownloader
import com.tbruyelle.rxpermissions2.RxPermissions
import com.uniondrug.udlib.web.R
import com.uniondrug.udlib.web.UDWebSDK
import com.uniondrug.udlib.web.bean.ShareBean
import com.uniondrug.udlib.web.common.EventBusMessage
import com.uniondrug.udlib.web.common.Extras
import com.uniondrug.udlib.web.common.JSConst
import com.uniondrug.udlib.web.utils.*
import com.uniondrug.udlib.web.widget.*
import kotlinx.android.synthetic.main.udweb_activity_ud_web_view.*
import kotlinx.android.synthetic.main.udweb_dialog_mp.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class UDWebViewActivity : AppCompatActivity(), View.OnClickListener, BridgeHandler {
    // 网页链接
    private lateinit var link: String
    private var mUrl: String = ""
    private lateinit var mMap: MutableMap<String, String>
    private var mCallBacks: MutableMap<String, CallBackFunction> = HashMap()
    private var shareBean: ShareBean? = null
    private var selfShareBean: ShareBean? = null//用于http或https使用
    private var mUploadCallBack: ValueCallback<Uri?>? = null
    private var mUploadCallBackAboveL: ValueCallback<Array<Uri>>? = null
    private var myRequest: PermissionRequest? = null
    private val IMG_TICKET = 113
    private val REQUEST_CODE_FILE_PDF = 114
    private var loadingDialog: LoadingDialog? = null
    private var TAG = "UDWEBVIEW_TAG"
    private var isMiniPMode = true
    private lateinit var netViewModel: UDWebViewModel
    private var uploadFileName = ""
    private var hasPicShare = false
    private var mpProject = ""
    private var fileName = ""
    private var isSingleMode = false
    val TXT = "text/plain"
    val PDF = "application/pdf"
    val DOC = "application/msword"
    val XLS = "application/vnd.ms-excel"
    val PPT = "application/vnd.ms-powerpoint"
    val DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    val XLS1 = "application/x-excel"
    val XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    val PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation"

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(
            R.anim.activity_open,
            R.anim.anim_no
        )

        AlexStatusBarUtils.setTransparentStatusBar(this, null)
        setContentView(R.layout.udweb_activity_ud_web_view)
        ll_root?.fitsSystemWindows = true
        StatusBarUtils.setStatusTextColor(true, this)
        StatusBarUtils.setColor(this, Color.WHITE, 0)
        rlLoading.visibility = View.VISIBLE
        imgBack.setOnClickListener(this)
        mpImgBack.setOnClickListener(this)
        mpImgClose.setOnClickListener(this)
        mpImgMore.setOnClickListener(this)
        tvReLoad.setOnClickListener(this)
        imgClose.setOnClickListener(this)
        rlShare.setOnClickListener(this)
        AndroidBug5497Workaround.assistActivity(this, webview)
        if (intent != null) {
            link = intent.getStringExtra("link") as String
            initWebView()
            if (link.startsWith("file")) {
                isMiniPMode = true
                miniHead.visibility = View.VISIBLE
                mpImgBack.visibility = View.VISIBLE
                oriHead.visibility = View.GONE
                mUrl = link
                showAndLoadUrl()
                return
            }
            mMap = intent.getSerializableExtra("mapData") as MutableMap<String, String>
            mMap["_t"] = System.currentTimeMillis().toString()
            mMap["_env"] = UDWebSDK.getInstance().environment.toString()

            netViewModel = ViewModelProvider(this).get(UDWebViewModel::class.java)

            if (link.startsWith("http") || link.startsWith("https")) {
                isMiniPMode = false
                oriHead.visibility = View.VISIBLE
                mUrl = link
                showAndLoadUrl()
                selfShareBean = ShareBean(mUrl, "", mUrl, "", "0")
            } else {
                isMiniPMode = true
                miniHead.visibility = View.VISIBLE
                oriHead.visibility = View.GONE
                //有Version 并且 有 Route 则不请求接口
                mpProject = mMap["mpProject"] as String
                val mpUrl = mMap["mpUrl"]
                var mpQuery = mMap["mpQuery"]
                if (mpQuery == null) mpQuery = ""
                val hasToken = mpQuery.contains("?token=") || mpQuery.contains("&token=")
                for (key in mMap.keys) {
                    if ("mpProject" == key || "mpVersion" == key || "mpUrl" == key || "mpQuery" == key || "mpRouter" == key) continue
                    if (hasToken && "token" == key) continue
                    mpQuery += if (mpQuery.isNotEmpty()) {
                        "&" + key + "=" + mMap[key]
                    } else {
                        key + "=" + mMap[key]
                    }
                    if ("hasHeadBar" == key) {
                        oriHead.visibility = if ("0" == mMap[key]) View.GONE else View.VISIBLE
                    }
                    if ("canShare" == key) {
                        rlShare.visibility = if ("1" == mMap[key]) View.VISIBLE else View.GONE
                    }
                }
                if (mpProject.isNotEmpty()) {
                    //请求版本信息
                    netViewModel.getVersions(mpProject, mMap["assistantId"])
                } else {
                    netViewModel.errorEvent.value = true
                    ToastUtils.showShortToast(this, "项目参数为空")
                }
                netViewModel.resLiveData.observe(this, androidx.lifecycle.Observer { versionBean ->
                    if (versionBean.unionToken.isNotEmpty()) {
                        val cookieList = ArrayList<String>()
                        cookieList.add("union_token=" + versionBean.unionToken)
                        syncCookie(".turboradio.cn", cookieList)
                        syncCookie(".uniondrug.net", cookieList)
                        syncCookie(".uniondrug.cn", cookieList)
                    }
                    fileName = versionBean.project + "_" + versionBean.version
                    //mpUrl
                    mUrl = if (mpUrl != null && mpUrl.isNotEmpty()) {
                        "file:$filesDir/$fileName/index.html#${mpUrl}" + "?" + mpQuery
                    } else {
                        "file:$filesDir/$fileName/index.html#${versionBean.router}" + "?" + mpQuery
                    }
                    val file = File("$filesDir/$fileName")
                    if (file.exists()) {
                        //直接加载本地资源
                        showAndLoadUrl()
                    } else {
                        //删除旧的
                        try {
                            val pres = getPreferences(MODE_PRIVATE)
                            val tempName = pres?.getString(mpProject, "")
                            if (tempName?.isNotEmpty() == true) {
                                val curFile = File("$filesDir/$tempName")
                                if (curFile.exists()) {
                                    recursionDeleteFile(curFile)
                                    with(pres.edit()) {
                                        putString(mpProject, "")
                                        commit()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                        }
                        downloadAndLoad(fileName, versionBean.downUrl)
                    }
                })
                netViewModel.errorEvent.observe(this, androidx.lifecycle.Observer {
                    llError.visibility = View.VISIBLE
                    webview?.visibility = View.GONE
                    rlLoading.visibility = View.GONE
                })
            }
            netViewModel.netUrlData.observe(this, androidx.lifecycle.Observer { url ->
                run {
                    hideLoadingDialog()
                    if (url.isNotEmpty()) {
                        val obj = JSONObject()
                        obj.put("data", url)
                        sendJSON2JS(JSConst.JSCONST_UDUPIMAGE, true, obj)
                    }
                }
            })
            netViewModel.fileUrlData.observe(this, androidx.lifecycle.Observer { url ->
                run {
                    hideLoadingDialog()
                    if (url.isNotEmpty()) {
                        val obj = JSONObject()
                        obj.put("data", url)
                        obj.put("name", uploadFileName)
                        sendJSON2JS(JSConst.JSCONST_UD_UPLOAD_FILE, true, obj)
                    }
                }
            })
        } else {
            //数据错误
            ToastUtils.showShortToast(this, "入参错误")
            finish()
        }

        RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { aBoolean: Boolean ->
            }
        EventBus.getDefault().register(this)
    }

    private fun showAndLoadUrl() {
        if (isMiniPMode) {
            oriHead.visibility = View.GONE
        }
        rlLoading.visibility = View.GONE
        Log.w("UDWEB->URL", "showAndLoadUrl: $mUrl")
        webview?.loadUrl(mUrl)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.mpImgBack, R.id.imgBack -> {
                onBackPressed()
            }
            R.id.mpImgClose, R.id.imgClose -> {
                finish()
            }
            R.id.mpImgMore -> {
                val dialog = MPDialog(this, shareBean != null, hasPicShare)
                dialog.setDialogClickListener(object : BaseAlertDialog.DialogClickListener {
                    override fun onClickListener(position: Int) {
                        var share: ShareBean? = selfShareBean
                        if (shareBean != null) share = shareBean
                        when (position) {
                            1 -> {
                                finish()
                                startActivity(intent)
                            }
                            2 -> {
                                sendWXShare(share, true)
                            }
                            3 -> {
                                sendWXShare(share, false)
                            }
                            4 -> {
                                webview?.callHandler(
                                    JSConst.JSCONST_UD_IMG_SHARE_WX,
                                    ""
                                ) {
                                }
                            }
                        }
                    }
                })
                dialog.show()
            }
            R.id.tvReLoad -> {
                finish()
                startActivity(intent)
            }
            R.id.rlShare -> {
                val shareDialog = WXShareDialog(this)
                shareDialog.setDialogClickListener(object : BaseAlertDialog.DialogClickListener {
                    override fun onClickListener(position: Int) {
                        var share: ShareBean? = selfShareBean
                        if (shareBean != null) share = shareBean
                        when (position) {
                            1 -> {
                                val content: String? = share?.shareLink
                                if (content != null && content.isNotEmpty()) {
                                    try {
                                        val cm =
                                            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val mClipData = ClipData.newPlainText("text", content)
                                        cm.primaryClip = mClipData
                                        ToastUtils.showShortToast(this@UDWebViewActivity, "成功")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            2 -> {
                                sendWXShare(share, true)
                            }
                            3 -> {
                                sendWXShare(share, false)
                            }
                        }
                    }
                })
                shareDialog.show()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webview?.removeJavascriptInterface("searchBoxJavaBridge_")
        webview?.removeJavascriptInterface("accessibility")
        webview?.removeJavascriptInterface("accessibilityTraversal")
//        webview?.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
//            downloadFile(url)
//        }
        if (webview == null) return
        val ws = webview!!.settings
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
//        bindingView.webview?.setInitialScale(1);
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
        webview?.webViewClient = object : BridgeWebViewClient(webview) {
//            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
//                return super.shouldInterceptRequest(view, request)
//            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                // 如下方案可在非微信内部WebView的H5页面中调出微信支付
                if (url.startsWith("weixin://wap/pay?")) {
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                    return true
                } else if (url.startsWith("uniondrugshop://app")) {
                    try {
                        val callBack = UDWebSDK.getInstance().getMPEventCallBack()
                        callBack?.onCallBack("innerLink", url)
                        return true
                    } catch (e: java.lang.Exception) {
                        Log.w(TAG, "shouldOverrideUrlLoading: " + e.message)
                    }
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.w(TAG, "" + webview?.canGoBack() + "onCreateWindow: onPageStarted->$url")

            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (isSingleMode) {
                    mpImgBack.visibility = View.VISIBLE
                } else {
                    if (webview == null) {
                        mpImgBack.visibility = View.GONE
                    } else {
                        mpImgBack.visibility =
                            if (webview!!.canGoBack()) View.VISIBLE else View.GONE
                    }
                }
                Log.w(TAG, "onCreateWindow: onPageFinished$url")
            }

            override fun onLoadResource(view: WebView, url: String) {
                super.onLoadResource(view, url)
                Log.w(TAG, "onCreateWindow: onPageStarted<-$url")
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                Log.w("network:", "onReceivedError: $failingUrl")
                llError.visibility = View.VISIBLE
                webview?.visibility = View.GONE
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
                    Log.w(TAG, "onReceivedError: " + error.errorCode + error.description)
                    Log.w(TAG, "onReceivedError: " + request.url.toString())
                }
            }
        }

        webview?.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
                toolbar_title.text = title
                mpTvTitle.text = title
                if (selfShareBean != null) selfShareBean?.shareTitle = title
            }

            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message
            ): Boolean {
                //html中调用window.open()，会回调此函数
                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = webview
                resultMsg.sendToTarget()
                return true
            }

            override fun onProgressChanged(view: WebView, newProgress: Int) {
//                progressBar.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
                progressBar.progress = newProgress
            }

            // For Android  >= 3.0
            fun openFileChooser(valueCallback: ValueCallback<Uri?>?, acceptType: String?) {
                mUploadCallBack = valueCallback
                takeImage()
            }

            //For Android  >= 4.1
            fun openFileChooser(
                valueCallback: ValueCallback<Uri?>?,
                acceptType: String?,
                capture: String?
            ) {
                mUploadCallBack = valueCallback
                takeImage()
            }

            // For Android >= 5.0
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                mUploadCallBackAboveL = filePathCallback
                takeImage()
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                myRequest = request
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    for (permission in request.resources) {
                        when (permission) {
                            "android.webkit.resource.AUDIO_CAPTURE" -> {
                                askForPermission(
                                    request.origin.toString(),
                                    Manifest.permission.RECORD_AUDIO,
                                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO
                                )
                            }
                        }
                    }
                }
            }
        }
        webview?.setOnLongClickListener {
            val hitTestResult = webview?.hitTestResult
            if (hitTestResult != null) {
                if (hitTestResult.type == WebView.HitTestResult.IMAGE_TYPE || hitTestResult.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    val pic = hitTestResult.extra //获取图片
                    Thread { saveImage(pic) }.start()
                }
            }
            true
        }
        // 与js交互
        webview?.setDefaultHandler(DefaultHandler())
        webview?.registerHandler(JSConst.JSCONST_UDUPIMAGE, this)
        webview?.registerHandler(JSConst.JSCONST_UDTEL, this)
        webview?.registerHandler(JSConst.JSCONST_UDSCAN, this)
        webview?.registerHandler(JSConst.JSCONST_UDUSERINFO, this)
        webview?.registerHandler(JSConst.JSCONST_UDVERSION, this)
        webview?.registerHandler(JSConst.JSCONST_UDBACKTOHOME, this)
        webview?.registerHandler(JSConst.JSCONST_UDDOWNIMAGE, this)
        webview?.registerHandler(JSConst.JSCONST_UDTONAVIGATE, this)
        webview?.registerHandler(JSConst.JSCONST_UDCOPY, this)
        webview?.registerHandler(JSConst.JSCONST_UDWXSHARE, this)
        webview?.registerHandler(JSConst.JSCONST_UDTOFEEDBACK, this)
        webview?.registerHandler(JSConst.JSCONST_UDSETHEADBARHIDDEN, this)
        webview?.registerHandler(JSConst.JSCONST_UDSETSHAREHIDDEN, this)
        webview?.registerHandler(JSConst.JSCONST_UDSERVICESHARE, this)
        webview?.registerHandler(JSConst.JSCONST_UDFINISH, this)
        webview?.registerHandler(JSConst.JSCONST_UDDTPLIST, this)
        webview?.registerHandler(JSConst.JSCONST_UDPOINTSFEEDBACK, this)
        webview?.registerHandler(JSConst.JSCONST_BACK_TO_MEMBER_ROOT, this)
        webview?.registerHandler(JSConst.JSCONST_TO_MEMBER, this)
        webview?.registerHandler(JSConst.JSCONST_UD_OPEN_LIVE, this)
        webview?.registerHandler(JSConst.JSCONST_UD_WATCH_LIVE, this)
        webview?.registerHandler(JSConst.JSCONST_UD_UPLOAD_FILE, this)
        webview?.registerHandler(JSConst.JSCONST_UD_AUTH, this)
        webview?.registerHandler(JSConst.JSCONST_UD_DOWNLOAD_FILE, this)
        webview?.registerHandler(JSConst.JSCONST_UD_MEDICINE_AUTH, this)
        webview?.registerHandler(JSConst.JSCONST_UD_SHOW_SHARE_WX, this)
        webview?.registerHandler(JSConst.JSCONST_UD_HIDE_SHARE_WX, this)
        webview?.registerHandler(JSConst.JSCONST_UD_PAY, this)
        webview?.registerHandler(JSConst.JSCONST_UD_WATCH_AGORA, this)
        webview?.registerHandler(JSConst.JSCONST_UDMPSHARE, this)
        webview?.registerHandler(JSConst.JSCONST_UDAPPTOJS, this)
        webview?.registerHandler(JSConst.JSCONST_UDJSTOAPP, this)
        webview?.registerHandler(JSConst.JSCONST_UDNAVIGATETO, this)
        webview?.registerHandler(JSConst.JSCONST_UDSHOWTAB, this)
        webview?.registerHandler(JSConst.JSCONST_UDTOORIGINVIEW, this)
        webview?.registerHandler(JSConst.JSCONST_UDRELOADDATA, this)
    }

    private fun saveImage(data: String) {
        try {
            val bitmap: Bitmap? = webData2bitmap(data)
            if (bitmap != null) {
                save2Album(bitmap, "img_" + System.currentTimeMillis().toString() + ".jpg")
            } else {
                runOnUiThread { Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT) }
            }
        } catch (e: java.lang.Exception) {
            runOnUiThread {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT)
            }
            e.printStackTrace()
        }
    }

    private fun webData2bitmap(data: String): Bitmap? {
        val imageBytes: ByteArray =
            decode(data.split(",".toRegex()).toTypedArray()[1], DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun save2Album(bitmap: Bitmap, fileName: String) {
        val file = File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM), fileName)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            runOnUiThread {
                sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(file)
                    )
                )
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT)
            }
        } catch (e: java.lang.Exception) {
            runOnUiThread {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT)
            }
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (ignored: java.lang.Exception) {
            }
        }
    }

    private fun showLoadingDialog(text: String = "加载中...") {
        try {
            if (loadingDialog == null) {
                loadingDialog = LoadingDialog(this, text)
                loadingDialog?.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog!!.hide()
            loadingDialog!!.dismiss()
            loadingDialog = null
        }
    }

    /**
     * 上传图片之后的回调
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode != Activity.RESULT_OK) {
            clearUploadMessage()
            return
        }

        when (requestCode) {
            IMG_TICKET -> {
                if (intent == null) {
                    ToastUtils.showShortToast(this, R.string.toast_get_image_error)
                    return
                }
                uploadImages(intent.getStringExtra(Extras.EXTRA_FILE_PATH))
            }
            REQUEST_CODE_FILE_CHOOSER -> {
                if (intent == null) {
                    Toast.makeText(
                        this,
                        R.string.picker_image_error, Toast.LENGTH_LONG
                    ).show()
                    clearUploadMessage()
                    return
                }
                val imagePath = intent.getStringExtra(Extras.EXTRA_FILE_PATH)
                if (!TextUtils.isEmpty(imagePath)) {
                    val f = File(imagePath)
                    if (f.exists() && f.isFile) {
                        val newUri = Uri.fromFile(f)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (mUploadCallBackAboveL != null) {
                                if (newUri != null) {
                                    mUploadCallBackAboveL!!.onReceiveValue(arrayOf(newUri))
                                    mUploadCallBackAboveL = null
                                    return
                                }
                            }
                        } else if (mUploadCallBack != null && newUri != null) {
                            mUploadCallBack!!.onReceiveValue(newUri)
                            mUploadCallBack = null
                            return
                        }
                    }
                }
                clearUploadMessage()
            }
            REQUEST_CODE_QRCODE -> {
                if (intent != null) {
                    val obj = JSONObject()
                    obj.put("data", intent.getStringExtra(Extras.EXTRA_FILE_PATH))
                    sendJSON2JS(JSConst.JSCONST_UDSCAN, true, obj)
                }
            }
            REQUEST_CODE_FILE_PDF -> {
                if (intent != null && intent.data != null) {
                    val fileBean = FileChooseUtils.getFile(this, intent.data)
                    Log.w("TAG", "UploadFile: path->" + fileBean.path)
                    var canUpload = false
                    val list =
                        arrayListOf(
                            ".pptx",
                            ".pdf",
                            ".doc",
                            ".docx",
                            ".xls",
                            ".xlsx",
                            ".ppt",
                            ".txt",
                            ".xlsx"
                        )
                    try {
                        val index = fileBean.path?.lastIndexOf(".")
                        val type = index?.let { fileBean.path?.substring(it)?.toLowerCase() }
                        if (type in list) canUpload = true
                    } catch (e: Exception) {
                        canUpload = false
                    }

                    if (canUpload) {
                        showLoadingDialog()
                        uploadFileName = if (!TextUtils.isEmpty(fileBean.name)) {
                            fileBean.name
                        } else {
                            FileChooseUtils.getFileName(fileBean.path!!)
                        }
                        netViewModel.uploadPDF(fileBean.path!!)
                    } else {
                        ToastUtils.showShortToast(this, "请上传pdf,ppt,word,excel,txt文件")
                    }
                }
            }
        }
    }

    /**
     * webview没有选择文件也要传null，防止下次无法执行
     */
    private fun clearUploadMessage() {
        if (mUploadCallBackAboveL != null) {
            mUploadCallBackAboveL!!.onReceiveValue(null)
            mUploadCallBackAboveL = null
        }
        if (mUploadCallBack != null) {
            mUploadCallBack!!.onReceiveValue(null)
            mUploadCallBack = null
        }
    }

    public override fun onPause() {
        super.onPause()
        webview?.onPause()
    }

    public override fun onResume() {
        super.onResume()
        webview?.onResume()
        webview?.resumeTimers()
        if (mCallBacks[JSConst.JSCONST_UD_AUTH] != null) {
            webview?.reload()
            mCallBacks.remove(JSConst.JSCONST_UD_AUTH)
        }
        if (mCallBacks[JSConst.JSCONST_UD_MEDICINE_AUTH] != null) {
            webview?.reload()
            mCallBacks.remove(JSConst.JSCONST_UD_MEDICINE_AUTH)
        }
        if (mCallBacks[JSConst.JSCONST_UD_OPEN_LIVE] != null) {
            webview?.reload()
            mCallBacks.remove(JSConst.JSCONST_UD_OPEN_LIVE)
        }
        if (mCallBacks[JSConst.JSCONST_UD_WATCH_LIVE] != null) {
            webview?.reload()
            mCallBacks.remove(JSConst.JSCONST_UD_WATCH_LIVE)
        }

        webview?.callHandler(JSConst.JSCONST_UDRELOADDATA, "") {}
    }

    override fun onDestroy() {
        super.onDestroy()
        llWebContent?.removeView(webview)
        webview?.removeAllViews()
        webview?.loadUrl("about:blank")
        webview?.stopLoading()
        webview?.webChromeClient = null
        webview?.destroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        if (webview == null) {
            finish()
        } else {
            if (webview!!.canGoBack()) {
                webview?.callHandler(JSConst.JSCONST_UDAPPTOJS, "{\"event\":\"UDBackPressed\"}") {}
                webview?.goBack()
            } else {
                finish()
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun takeImage() {
        RxPermissions(this).request(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).subscribe { aBoolean: Boolean ->
            if (aBoolean) {
                showFileChooser()
            } else {
                Toast.makeText(application, "请同意拍照相关权限", Toast.LENGTH_SHORT)
            }
        }
    }

    private fun loadNewUrl(url: String) {
        llError.visibility = View.GONE
        webview?.loadUrl(url)
        webview?.visibility = View.VISIBLE
    }

    /**
     * 打开选择文件/相机
     */
    private fun showFileChooser() {
        val intent = Intent(this, PickImageActivity::class.java)
        val path =
            StorageUtils.getWritePath(StringUtils.get32UUID() + ".jpg", StorageType.TYPE_TEMP)
        intent.putExtra(Extras.EXTRA_FROM, Extras.FROM_LOCAL)
        intent.putExtra(Extras.EXTRA_FILE_PATH, path)
        intent.putExtra(Extras.EXTRA_CROP_TYPE, '1')
        startActivityForResult(intent, REQUEST_CODE_FILE_CHOOSER)
    }

    private fun sendJSON2JS(event: String, success: Boolean, str: String) {
        if (mCallBacks[event] != null) {
            try {
                val toJSBean = JSONObject()
                toJSBean.put("status", if (success) "1" else "0")
                toJSBean.put("msg", str)
                Log.w("jsPlatform", "send: $toJSBean")
                mCallBacks[event]?.onCallBack(toJSBean.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendJSON2JS(event: String, success: Boolean, obj: JSONObject) {
        if (mCallBacks[event] != null) {
            try {
                val toJSBean = JSONObject()
                toJSBean.put("status", if (success) "1" else "0")
                toJSBean.put("data", obj)
                Log.w("jsPlatform", "send: $toJSBean")
                mCallBacks[event]?.onCallBack(toJSBean.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun handler(data: String, function: CallBackFunction) {
        if (data.isEmpty()) return
        Log.w("jsPlatform", "receive: $data")
        val jsonObj = JSONUtils.stringToObj(data) ?: return
        val type = JSONUtils.getStrFromObj(jsonObj, "type")
        val name = JSONUtils.getStrFromObj(jsonObj, "name")
        if (name.isEmpty()) return
        mCallBacks[name] = function
        when (name) {
            JSConst.JSCONST_UDUPIMAGE -> {
                if (type.isEmpty()) return
                toTakeImage(type, JSONUtils.getStrFromObj(jsonObj, "crop"))
            }
            JSConst.JSCONST_UDTEL -> {
                val tel = JSONUtils.getStrFromObj(jsonObj, "tel")
                if (tel.isEmpty()) return
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tel:$tel"))
                startActivity(intent)
            }
            JSConst.JSCONST_UDSCAN -> {
                val dis = RxPermissions(this).request(
                    Manifest.permission.CAMERA
                ).subscribe { aBoolean: Boolean ->
                    if (aBoolean) {
                        //初始化
                        val map: MutableMap<String, String> = HashMap()
                        map["scanTitle"] = ""
                        map["scanFootCon"] = ""
                        map["scanMidCon"] = ""
                        when (type) {
                            "1" -> {
                                map["scanTitle"] = "扫描顾客权益码"
                            }
                            "2" -> {
                                map["scanTitle"] = "扫描顾客付款码"
                                map["scanFootCon"] =
                                    "1、请顾客关注打开“药联健康服务公众号”\n2、打开个人中心，出示个人二维码\n3、扫描顾客个人二维码"
                            }
                            "3" -> {
                                map["scanTitle"] = JSONUtils.getStrFromObj(jsonObj, "scanTitle")
                                map["scanMidCon"] = JSONUtils.getStrFromObj(jsonObj, "scanMidCon")
                                map["scanFootCon"] = JSONUtils.getStrFromObj(jsonObj, "scanFootCon")
                            }
                        }
                        QRCodeActivity.start(this, REQUEST_CODE_QRCODE, map)
                    } else {
                        ToastUtils.showShortToast(application, "无摄像头权限")
                    }
                }
            }
            JSConst.JSCONST_UDBACKTOHOME -> {
                val callBack = UDWebSDK.getInstance().getMPEventCallBack()
                callBack?.onCallBack(name, data)
                finish()
            }
            JSConst.JSCONST_UDVERSION,
            JSConst.JSCONST_UDUSERINFO,
            JSConst.JSCONST_UDTOFEEDBACK,
            JSConst.JSCONST_UDDTPLIST,
            JSConst.JSCONST_BACK_TO_MEMBER_ROOT,
            JSConst.JSCONST_UD_OPEN_LIVE,
            JSConst.JSCONST_UD_WATCH_LIVE,
            JSConst.JSCONST_UD_AUTH,
            JSConst.JSCONST_UD_MEDICINE_AUTH,
            JSConst.JSCONST_UD_PAY,
            JSConst.JSCONST_UD_WATCH_AGORA,
            JSConst.JSCONST_UDMPSHARE,
            JSConst.JSCONST_UDSHOWTAB,
            JSConst.JSCONST_UDTOORIGINVIEW,
            JSConst.JSCONST_TO_MEMBER -> {
                val callBack = UDWebSDK.getInstance().getMPEventCallBack()
                callBack?.onCallBack(name, data)
            }
            JSConst.JSCONST_UDDOWNIMAGE -> {
                RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe { aBoolean: Boolean ->
                        if (aBoolean) {
                            val base64 = JSONUtils.getStrFromObj(jsonObj, "base64")
                            if (base64.isEmpty()) {
                                sendJSON2JS(name, false, "无图片数据")
                            } else {
                                FileUtils.saveImage(this@UDWebViewActivity, base64)
                                sendJSON2JS(name, true, "成功")
                                ToastUtils.showShortToast(this, "成功")
                            }
                        } else {
                            sendJSON2JS(name, false, "无存储权限")
                        }
                    }
            }
            JSConst.JSCONST_UDTONAVIGATE -> {
                val latitude = JSONUtils.getStrFromObj(jsonObj, "latitude")
                val longitude = JSONUtils.getStrFromObj(jsonObj, "longitude")
                val address = JSONUtils.getStrFromObj(jsonObj, "address")
                try {
                    val lat = latitude.toDouble()
                    val lon = longitude.toDouble()
                    toNavigate(lat, lon, address)
                } catch (e: Exception) {
                    sendJSON2JS(name, false, "经纬度错误")
                    e.printStackTrace()
                    return
                }
            }
            JSConst.JSCONST_UDCOPY -> {
                val content = JSONUtils.getStrFromObj(jsonObj, "content")
                if (content.isEmpty()) {
                    sendJSON2JS(name, false, "内容有误")
                    return
                }
                try {
                    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val mClipData = ClipData.newPlainText("text", content)
                    cm.primaryClip = mClipData
                    sendJSON2JS(name, true, "成功")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            JSConst.JSCONST_UDSERVICESHARE -> {
                shareBean = ShareBean(
                    JSONUtils.getStrFromObj(jsonObj, "shareLink"),
                    JSONUtils.getStrFromObj(jsonObj, "shareTitle"),
                    JSONUtils.getStrFromObj(jsonObj, "shareDescr"),
                    JSONUtils.getStrFromObj(jsonObj, "shareImg"),
                    JSONUtils.getStrFromObj(jsonObj, "shareBigImg")
                )
                rlShare.visibility = View.VISIBLE
            }
            JSConst.JSCONST_UDWXSHARE -> {
                shareBean = ShareBean(
                    JSONUtils.getStrFromObj(jsonObj, "shareLink"),
                    JSONUtils.getStrFromObj(jsonObj, "shareTitle"),
                    JSONUtils.getStrFromObj(jsonObj, "shareDescr"),
                    JSONUtils.getStrFromObj(jsonObj, "shareImg"),
                    JSONUtils.getStrFromObj(jsonObj, "shareBigImg")
                )
                sendWXShare(shareBean, "1" == JSONUtils.getStrFromObj(jsonObj, "shareType"))
            }
            JSConst.JSCONST_UDFINISH -> {
                finish()
            }
            JSConst.JSCONST_UDSETHEADBARHIDDEN -> {
                if (isMiniPMode) {
                    miniHead.visibility = if ("1" == type) View.GONE else View.VISIBLE
                } else {
                    oriHead.visibility = if ("1" == type) View.GONE else View.VISIBLE
                }
            }
            JSConst.JSCONST_UDSETSHAREHIDDEN -> {
                rlShare.visibility = if ("1" == type) View.GONE else View.VISIBLE
            }
            JSConst.JSCONST_UDPOINTSFEEDBACK -> {

            }
            JSConst.JSCONST_UDJSTOAPP -> {
                val callBack = UDWebSDK.getInstance().getMPEventCallBack()
                callBack?.onCallBack(JSConst.JSCONST_UDJSTOAPP, data)
            }
            JSConst.JSCONST_UD_UPLOAD_FILE -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                val mimeTypes = arrayOf(DOC, DOCX, PDF, PPT, PPTX, XLS, XLS1, XLSX, TXT)
                intent.type = "application/*"

                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
//                intent.type = "application/pdf"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent, REQUEST_CODE_FILE_PDF)
            }
            JSConst.JSCONST_UD_DOWNLOAD_FILE -> {
                //下载
                val fileName = JSONUtils.getStrFromObj(jsonObj, "fileName")
                val url = JSONUtils.getStrFromObj(jsonObj, "url")
                val filePath = getExternalStorageDirectory().absolutePath + "/" + fileName
                downloadFile(url, filePath)
            }
            JSConst.JSCONST_UD_SHOW_SHARE_WX -> {
                hasPicShare = true
            }
            JSConst.JSCONST_UD_HIDE_SHARE_WX -> {
                hasPicShare = false
            }
            JSConst.JSCONST_UDNAVIGATETO -> {
                val intent = Intent(this, UDWebViewActivity::class.java)
                intent.putExtra("url", fileName + JSONUtils.getStrFromObj(jsonObj, "url"))
                startActivity(intent)
            }
        }
    }

    fun askForPermission(origin: String, permission: String, requestCode: Int) {
        Log.d("WebView", "inside askForPermission for" + origin + "with" + permission)
        if (ContextCompat.checkSelfPermission(applicationContext, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, permission
                )
            ) {
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this, arrayOf(permission),
                    requestCode
                )
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                myRequest!!.grant(myRequest!!.resources)
            }
        }
    }

    private fun toNavigate(latitude: Double, longitude: Double, address: String) {
        val hasBaidu = PackageManagerUtils.haveBaiduMap(packageManager)
        val hasGaode = PackageManagerUtils.haveGaodeMap(packageManager)
        val list: MutableList<String> = ArrayList()
        if (hasBaidu) {
            list.add("百度地图")
        }
        if (hasGaode) {
            list.add("高德地图")
        }
        if (!hasBaidu && !hasGaode) {
            list.add("高德地图")
        }
        MultiChoosePopUp(this@UDWebViewActivity, list, "请选择地图",
            object : MultiChoosePopUp.ItemClickCallback {
                override fun onClick(position: Int) {
                    if (list[position] == "百度地图") {
                        MapUtils.openBaiduMapToGuide(
                            this@UDWebViewActivity, latitude,
                            longitude, address
                        )
                    } else if (list[position] == "高德地图") {
                        if (hasGaode) {
                            MapUtils.openGaodeMapToGuide(
                                this@UDWebViewActivity,
                                latitude.toString(),
                                longitude.toString(),
                                address
                            )
                        } else {
                            MapUtils.openBrowserToGuide(
                                this@UDWebViewActivity,
                                latitude.toString(),
                                longitude.toString(),
                                address
                            )
                        }
                    }
                }
            }
        ).showAtLocation(ll_root, Gravity.CENTER, 0, 0)
    }

    @SuppressLint("CheckResult")
    private fun toTakeImage(type: String, crop: String) {
        var canCamera = true
        var canPhoto = true
        var cropType = "1"
        if (crop != "1" || crop != "2" || crop != "3") cropType = "1"
        when (type) {
            "1" -> {
            }
            "2" -> {
                canPhoto = false
            }
            "3" -> {
                canCamera = false
            }
        }

        RxPermissions(this).request(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
            .subscribe { aBoolean: Boolean ->
                if (aBoolean) {
                    //用户已经同意所有该权限
                    val list: MutableList<String> = ArrayList()
                    if (canCamera) list.add("拍照")
                    if (canPhoto) list.add("从手机相册选取")
                    MultiChoosePopUp(
                        this,
                        list,
                        "",
                        object : MultiChoosePopUp.ItemClickCallback {
                            override fun onClick(position: Int) {
                                when (list[position]) {
                                    "拍照" -> {
                                        val filename = StringUtils.get32UUID() + ".jpg"
                                        val path =
                                            StorageUtils.getWritePath(
                                                filename,
                                                StorageType.TYPE_TEMP
                                            )
                                        val intent = Intent(
                                            this@UDWebViewActivity,
                                            PickImageActivity::class.java
                                        )
                                        intent.putExtra(Extras.EXTRA_FROM, Extras.FROM_CAMERA)
                                        intent.putExtra(Extras.EXTRA_FILE_PATH, path)
                                        intent.putExtra(Extras.EXTRA_CROP_TYPE, cropType)
                                        startActivityForResult(intent, IMG_TICKET)
                                    }
                                    "从手机相册选取" -> {
                                        val filename = StringUtils.get32UUID() + ".jpg"
                                        val path =
                                            StorageUtils.getWritePath(
                                                filename,
                                                StorageType.TYPE_TEMP
                                            )

                                        val intent = Intent(
                                            this@UDWebViewActivity,
                                            PickImageActivity::class.java
                                        )
                                        intent.putExtra(Extras.EXTRA_FROM, Extras.FROM_LOCAL)
                                        intent.putExtra(Extras.EXTRA_FILE_PATH, path)
                                        intent.putExtra(Extras.EXTRA_CROP_TYPE, cropType)
                                        startActivityForResult(intent, IMG_TICKET)
                                    }
                                }
                            }
                        }).showAtLocation(ll_root, Gravity.BOTTOM, 0, 0)
                } else {
                    ToastUtils.showShortToast(this, "请同意拍照相关权限")
                }
            }
    }

    companion object {
        private const val REQUEST_CODE_FILE_CHOOSER = 336
        const val MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101
        const val REQUEST_CODE_QRCODE = 102
    }

    private fun uploadImages(imagePath: String?) {
        if (imagePath == null || imagePath.isEmpty()) {
            ToastUtils.showShortToast(this, "获取图片出错")
            return
        }
        netViewModel.uploadImage(imagePath)
        showLoadingDialog()
    }

    private fun downloadFile(fileUrl: String, path: String) {
        RxPermissions(this).request(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
            .subscribe { aBoolean: Boolean ->
                if (aBoolean) {
                    showLoadingDialog("下载中")
                    FileDownloader.setup(this)
                    FileDownloader.getImpl()
                        .create(fileUrl)
                        .setPath(path, false)
                        .setCallbackProgressTimes(300)
                        .setMinIntervalUpdateSpeed(400)
                        .setListener(object : FileDownloadSampleListener() {

                            override fun warn(task: BaseDownloadTask?) {
                                Log.w("UDWEB", "completed:")
                                hideLoadingDialog()
                            }

                            override fun error(task: BaseDownloadTask?, e: Throwable?) {
                                Log.w("UDWEB", "completed:" + e?.message)
                                ToastUtils.showShortToast(this@UDWebViewActivity, e?.message)
                                hideLoadingDialog()
                            }

                            override fun completed(task: BaseDownloadTask?) {
                                super.completed(task)
                                ToastUtils.showShortToast(this@UDWebViewActivity, "下载完成")
                                hideLoadingDialog()
                            }
                        }).start()
                } else {
                    ToastUtils.showShortToast(this, "无存储权限")
                }
            }
    }

    private fun downloadAndLoad(fileName: String, zipUrl: String) {
        if (zipUrl.isEmpty()) {
            ToastUtils.showShortToast(this, "下载链接错误")
            return
        }
        val zipFile = "$filesDir/$fileName.zip"
        FileDownloader.setup(this)
        FileDownloader.getImpl()
            .create(zipUrl)
            .setPath(zipFile, false)
            .setCallbackProgressTimes(300)
            .setMinIntervalUpdateSpeed(400)
            .setListener(object : FileDownloadSampleListener() {

                override fun warn(task: BaseDownloadTask?) {
                    Log.w("UDWEB", "completed:")
                }

                override fun error(task: BaseDownloadTask?, e: Throwable?) {
                    Log.w("UDWEB", "completed:" + e?.message)
                    netViewModel.errorEvent.value = true
                    ToastUtils.showShortToast(this@UDWebViewActivity, e?.message)
                }

                override fun completed(task: BaseDownloadTask?) {
                    super.completed(task)
                    try {
                        //3.解压缩
                        val mZipFile = File(zipFile)
                        UnzipUtils.unzip(mZipFile, File(filesDir.toString() + ""))
                        val oldFile = File("$filesDir/dist")
                        val newFile = File("$filesDir/$fileName")
                        oldFile.renameTo(newFile)
                        mZipFile.delete()
                        showAndLoadUrl()
                        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
                        with(sharedPref.edit()) {
                            putString(mMap["mpProject"], fileName)
                            commit()
                        }
//                        val sharedPref = getSharedPreferences(mpProject, Context.MODE_PRIVATE)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        netViewModel.errorEvent.value = true
                    }
                }
            }).start()
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(
            R.anim.anim_no,
            R.anim.activity_close
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EventBusMessage<*>) {
        /* Do something */
        when (event.msgType) {
            EventBusMessage.MSG_TYPE_APP_TO_MP -> {
                var name = ""
                name = try {
                    event.data as String
                } catch (e: Exception) {
                    ""
                }

                val json = JSONObject()
                json.put("event", event.data)
                json.put("data", event.extraData)
                Log.w(TAG, "onMessageEvent: $json")
                if (name.isNotEmpty() && mCallBacks[name] != null) {
                    mCallBacks[name]?.onCallBack(event.extraData)
                } else {
                    webview?.callHandler(
                        JSConst.JSCONST_UDAPPTOJS,
                        json.toString()
                    ) {
                    }
                }
            }
            EventBusMessage.MSG_TYPE_APP_FINISH -> {
                finish()
            }
            EventBusMessage.MSG_TYPE_APP_REFRESH -> {
                webview?.reload()
            }
        }
    }

    private fun sendWXShare(bean: ShareBean?, isWxSession: Boolean) {
        if (bean != null) {
            val callBack = UDWebSDK.getInstance().getMPEventCallBack()
            val json = JSONObject()
            json.put("shareLink", bean.shareLink)
            json.put("shareTitle", bean.shareTitle)
            json.put("shareDescr", bean.shareDescr)
            json.put("shareImg", bean.shareImg)
            json.put("shareBigImg", bean.shareBigImg)
            json.put("wxSession", if (isWxSession) "1" else "0")
            callBack?.onCallBack(JSConst.JSCONST_UDSERVICESHARE, json.toString())
        } else {
            ToastUtils.showLongToast(this, "未设置分享内容")
        }
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param file
     *            要删除的根目录
     */

    private fun recursionDeleteFile(file: File) {
        if (file.isFile) {
            file.delete()
            return
        }
        if (file.isDirectory) {
            val childFile = file.listFiles()
            if (childFile == null || childFile.isEmpty()) {
                file.delete()
                return
            }
            for (f in childFile) {
                recursionDeleteFile(f)
            }
            file.delete();
        }
    }


    private fun syncCookie(url: String, cookieList: ArrayList<String>?) {
        CookieSyncManager.createInstance(this)
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        if (cookieList != null) {
            for (cookie in cookieList) {
                cookieManager.setCookie(url, cookie)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush()
        } else {
            CookieSyncManager.getInstance().sync()
        }
    }
}
