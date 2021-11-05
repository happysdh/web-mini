package com.uniondrug.udlib.web.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

class UDWebFragment : Fragment(), View.OnClickListener, BridgeHandler {
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
    val TXT = "text/plain"
    val PDF = "application/pdf"
    val DOC = "application/msword"
    val XLS = "application/vnd.ms-excel"
    val PPT = "application/vnd.ms-powerpoint"
    val DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    val XLS1 = "application/x-excel"
    val XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    val PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    lateinit var oriHead: ConstraintLayout
    lateinit var miniHead: ConstraintLayout
    lateinit var ll_root: ConstraintLayout
    lateinit var rlLoading: RelativeLayout
    lateinit var mpImgClose: RelativeLayout
    lateinit var mpImgMore: RelativeLayout
    lateinit var imgClose: RelativeLayout
    lateinit var rlShare: RelativeLayout
    lateinit var imgBack: ImageView
    lateinit var mpImgBack: ImageView
    lateinit var tvReLoad: TextView
    lateinit var toolbar_title: TextView
    lateinit var mpTvTitle: TextView
    lateinit var webview: CustomWebView
    lateinit var llError: LinearLayout
    lateinit var progressBar: ProgressBar
    lateinit var llContent: LinearLayout
    companion object {
        private const val EXTRA_URL = "link"
        private const val EXTRA_MP_DATA = "mapData"
        private const val REQUEST_CODE_FILE_CHOOSER = 336
        const val MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101
        const val REQUEST_CODE_QRCODE = 102

        fun newInstance(url: String, map: MutableMap<String, String>): UDWebFragment {
            val uri = Uri.parse(url)
            if (uri.scheme != "http" && uri.scheme != "https" && uri.scheme != "uniondrugshop") {
//                ToastUtils.showLongToast(, "入参错误")
//                return
            }
            for (key in uri.queryParameterNames) {
                map[key] = uri.getQueryParameter(key)
            }
            return UDWebFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_URL, url)
                    putSerializable(EXTRA_MP_DATA, map as Serializable)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.w(TAG, "->onCreateView: ")
        val root = inflater.inflate(R.layout.udweb_activity_ud_web_view, container, false)
        oriHead = root.findViewById(R.id.oriHead)
        miniHead = root.findViewById(R.id.miniHead)
        rlLoading = root.findViewById(R.id.rlLoading)
        imgBack = root.findViewById(R.id.imgBack)
        mpImgBack = root.findViewById(R.id.mpImgBack)
        mpImgClose = root.findViewById(R.id.mpImgClose)
        mpImgMore = root.findViewById(R.id.mpImgMore)
        tvReLoad = root.findViewById(R.id.tvReLoad)
        imgClose = root.findViewById(R.id.imgClose)
        rlShare = root.findViewById(R.id.rlShare)
        webview = root.findViewById(R.id.webview)
        llError = root.findViewById(R.id.llError)
        mpTvTitle = root.findViewById(R.id.mpTvTitle)
        toolbar_title = root.findViewById(R.id.toolbar_title)
        progressBar = root.findViewById(R.id.progressBar)
        ll_root = root.findViewById(R.id.ll_root)
        llContent = root.findViewById(R.id.llWebContent)
        return root
    }

    override fun onStart() {
        super.onStart()
        Log.w(TAG, "->onStart: ")
    }

    @SuppressLint("CheckResult")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.w(TAG, "->onActivityCreated: ")
        oriHead.visibility = View.GONE
        miniHead.visibility = View.GONE
        rlLoading.visibility = View.VISIBLE
        imgBack.setOnClickListener(this)
        mpImgBack.setOnClickListener(this)
        mpImgClose.setOnClickListener(this)
        mpImgMore.setOnClickListener(this)
        tvReLoad.setOnClickListener(this)
        imgClose.setOnClickListener(this)
        rlShare.setOnClickListener(this)
        AndroidBug5497Workaround.assistActivity(requireActivity(), webview)

        if (arguments != null) {
            link = arguments!!.getString(EXTRA_URL) as String

            initWebView()
            if (link.startsWith("file")) {
                isMiniPMode = true
                miniHead.visibility = View.VISIBLE
                oriHead.visibility = View.GONE
                mUrl = link
                showAndLoadUrl()
                return
            }

            mMap = arguments!!.getSerializable(EXTRA_MP_DATA) as MutableMap<String, String>
            mMap["_t"] = System.currentTimeMillis().toString()
            mMap["_env"] = UDWebSDK.getInstance().environment.toString()

            netViewModel = ViewModelProvider(requireActivity()).get(UDWebViewModel::class.java)
            if (link.startsWith("http") || link.startsWith("https")) {
                isMiniPMode = false
                mUrl = link
                showAndLoadUrl()
                selfShareBean = ShareBean(mUrl, "", mUrl, "", "0")
            } else {
                isMiniPMode = true

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
                    if ("canShare" == key) {
                        rlShare.visibility = if ("1" == mMap[key]) View.VISIBLE else View.GONE
                    }
                }
                if (mpProject.isNotEmpty()) {
                    //请求版本信息
                    netViewModel.getVersions(mpProject, mMap["assistantId"])
                } else {
                    netViewModel.errorEvent.value = true
                    ToastUtils.showShortToast(activity, "项目参数为空")
                }
                netViewModel.resLiveData.observe(
                    requireActivity(),
                    androidx.lifecycle.Observer { versionBean ->
                        if (versionBean.unionToken.isNotEmpty()) {
                            val cookieList = ArrayList<String>()
                            cookieList.add("union_token=" + versionBean.unionToken)
                            syncCookie(".turboradio.cn", cookieList)
                            syncCookie(".uniondrug.net", cookieList)
                            syncCookie(".uniondrug.cn", cookieList)
                        }
                        fileName = versionBean.project + "_" + versionBean.version
                        //mpUrl
                        val filesDir = requireActivity().filesDir
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
                                val pres =
                                    requireActivity().getPreferences(AppCompatActivity.MODE_PRIVATE)
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
                netViewModel.errorEvent.observe(requireActivity(), androidx.lifecycle.Observer {
                    llError.visibility = View.VISIBLE
                    webview?.visibility = View.GONE
                    rlLoading.visibility = View.GONE
                })
            }
            netViewModel.netUrlData.observe(requireActivity(), androidx.lifecycle.Observer { url ->
                run {
                    hideLoadingDialog()
                    if (url.isNotEmpty()) {
                        val obj = JSONObject()
                        obj.put("data", url)
                        sendJSON2JS(JSConst.JSCONST_UDUPIMAGE, true, obj)
                    }
                }
            })
            netViewModel.fileUrlData.observe(requireActivity(), androidx.lifecycle.Observer { url ->
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
            ToastUtils.showShortToast(activity, "入参错误")
        }

        RxPermissions(requireActivity()).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { aBoolean: Boolean ->
            }
        EventBus.getDefault().register(this)
    }

    private fun showAndLoadUrl() {
        rlLoading.visibility = View.GONE
        Log.w("UDWEB->URL", "showAndLoadUrl: $mUrl")
        webview?.loadUrl(mUrl)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.mpImgBack, R.id.imgBack -> {

            }
            R.id.mpImgClose, R.id.imgClose -> {

            }
            R.id.mpImgMore -> {
                val dialog = MPDialog(requireContext(), shareBean != null, hasPicShare)
                dialog.setDialogClickListener(object : BaseAlertDialog.DialogClickListener {
                    override fun onClickListener(position: Int) {
                        var share: ShareBean? = selfShareBean
                        if (shareBean != null) share = shareBean
                        when (position) {
                            1 -> {
//                                startActivity(intent)
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
                rlLoading.visibility = View.VISIBLE
                netViewModel.getVersions(mpProject, mMap["assistantId"])
//                startActivity(intent)
            }
            R.id.rlShare -> {
                val shareDialog = WXShareDialog(requireActivity())
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
                                            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val mClipData = ClipData.newPlainText("text", content)
                                        cm.primaryClip = mClipData
                                        ToastUtils.showShortToast(requireActivity(), "成功")
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
                Log.w(TAG, "" + webview?.canGoBack() + "onPageStarted: onPageStarted->$url")

            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (webview == null) {
                    mpImgBack.visibility = View.GONE
                } else {
                    mpImgBack.visibility = if (webview!!.canGoBack()) View.VISIBLE else View.GONE
                }
                Log.w(TAG, "onPageFinished: onPageFinished$url")
            }

            override fun onLoadResource(view: WebView, url: String) {
                super.onLoadResource(view, url)
                Log.w(TAG, "onLoadResource: onPageStarted<-$url")
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
                try {
                    progressBar?.progress = newProgress
                } catch (e: Exception) {
                }
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
    }

    private fun saveImage(data: String) {
        try {
            val bitmap: Bitmap? = webData2bitmap(data)
            if (bitmap != null) {
                save2Album(bitmap, "img_" + System.currentTimeMillis().toString() + ".jpg")
            } else {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireActivity(), "保存成功", Toast.LENGTH_SHORT)
                }
            }
        } catch (e: java.lang.Exception) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireActivity(), "保存失败", Toast.LENGTH_SHORT)
            }
            e.printStackTrace()
        }
    }

    fun webData2bitmap(data: String): Bitmap? {
        val imageBytes: ByteArray =
            Base64.decode(data.split(",".toRegex()).toTypedArray()[1], Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun save2Album(bitmap: Bitmap, fileName: String) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            fileName
        )
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            requireActivity().runOnUiThread {
                requireActivity().sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(file)
                    )
                )
                Toast.makeText(requireActivity(), "保存成功", Toast.LENGTH_SHORT)
            }
        } catch (e: java.lang.Exception) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireActivity(), "保存失败", Toast.LENGTH_SHORT)
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
                loadingDialog = LoadingDialog(requireContext(), text)
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
                    ToastUtils.showShortToast(requireContext(), R.string.toast_get_image_error)
                    return
                }
                uploadImages(intent.getStringExtra(Extras.EXTRA_FILE_PATH))
            }
            REQUEST_CODE_FILE_CHOOSER -> {
                if (intent == null) {
                    Toast.makeText(
                        requireContext(),
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
                    val fileBean = FileChooseUtils.getFile(requireContext(), intent.data)
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
                        ToastUtils.showShortToast(requireContext(), "请上传pdf,ppt,word,excel,txt文件")
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

    override fun onStop() {
        super.onStop()
        Log.w(TAG, "->onStop: ")
    }

    override fun onPause() {
        super.onPause()
        Log.w(TAG, "->onPause: ")
        webview?.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.w(TAG, "->onDestroyView: ")
        if (webview != null) {
            llContent.removeView(webview)
            webview?.removeAllViews()
            webview?.loadUrl("about:blank")
            webview?.stopLoading()
            webview?.webChromeClient = null
            webview?.destroy()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.w(TAG, "->onResume: ")
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
    }

    override fun onDetach() {
        super.onDetach()
        Log.w(TAG, "->onDetach: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "->onDestroy: ")
        if (webview != null) {
            llContent.removeView(webview)
            webview?.removeAllViews()
            webview?.loadUrl("about:blank")
            webview?.stopLoading()
            webview?.webChromeClient = null
            webview?.destroy()
        }
        EventBus.getDefault().unregister(this)
    }
//
//    override fun onBackPressed() {
//        if (webview?.canGoBack()) {
//            webview?.callHandler(JSConst.JSCONST_UDAPPTOJS, "{\"event\":\"UDBackPressed\"}") {}
//            webview?.goBack()
//        } else {
//            finish()
//        }
//    }

    @SuppressLint("CheckResult")
    private fun takeImage() {
        RxPermissions(requireActivity()).request(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).subscribe { aBoolean: Boolean ->
            if (aBoolean) {
                showFileChooser()
            } else {
                Toast.makeText(requireContext(), "请同意拍照相关权限", Toast.LENGTH_SHORT)
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
        val intent = Intent(requireContext(), PickImageActivity::class.java)
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
                val dis = RxPermissions(requireActivity()).request(
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
                        QRCodeActivity.start(requireActivity(), REQUEST_CODE_QRCODE, map)
                    } else {
                        ToastUtils.showShortToast(requireContext(), "无摄像头权限")
                    }
                }
            }
            JSConst.JSCONST_UDBACKTOHOME -> {
                val callBack = UDWebSDK.getInstance().getMPEventCallBack()
                callBack?.onCallBack(name, data)
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
                RxPermissions(requireActivity()).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe { aBoolean: Boolean ->
                        if (aBoolean) {
                            val base64 = JSONUtils.getStrFromObj(jsonObj, "base64")
                            if (base64.isEmpty()) {
                                sendJSON2JS(name, false, "无图片数据")
                            } else {
                                FileUtils.saveImage(requireActivity(), base64)
                                sendJSON2JS(name, true, "成功")
                                ToastUtils.showShortToast(requireContext(), "成功")
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
                    val cm =
                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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

            }
            JSConst.JSCONST_UDSETHEADBARHIDDEN -> {

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
                val filePath =
                    Environment.getExternalStorageDirectory().absolutePath + "/" + fileName
                downloadFile(url, filePath)
            }
            JSConst.JSCONST_UD_SHOW_SHARE_WX -> {
                hasPicShare = true
            }
            JSConst.JSCONST_UD_HIDE_SHARE_WX -> {
                hasPicShare = false
            }
            JSConst.JSCONST_UDNAVIGATETO -> {
                val intent = Intent(requireActivity(), UDWebViewActivity::class.java)
                intent.putExtra("url", fileName + JSONUtils.getStrFromObj(jsonObj, "url"))
                startActivity(intent)
            }
            JSConst.JSCONST_UDRELOADDATA -> {

            }
        }
    }

    fun askForPermission(origin: String, permission: String, requestCode: Int) {
        Log.d("WebView", "inside askForPermission for" + origin + "with" + permission)
        if (ContextCompat.checkSelfPermission(requireContext(), permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(), permission
                )
            ) {
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(permission),
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
        val hasBaidu = PackageManagerUtils.haveBaiduMap(requireContext().packageManager)
        val hasGaode = PackageManagerUtils.haveGaodeMap(requireContext().packageManager)
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
        MultiChoosePopUp(requireActivity(), list, "请选择地图",
            object : MultiChoosePopUp.ItemClickCallback {
                override fun onClick(position: Int) {
                    if (list[position] == "百度地图") {
                        MapUtils.openBaiduMapToGuide(
                            requireActivity(), latitude,
                            longitude, address
                        )
                    } else if (list[position] == "高德地图") {
                        if (hasGaode) {
                            MapUtils.openGaodeMapToGuide(
                                requireActivity(),
                                latitude.toString(),
                                longitude.toString(),
                                address
                            )
                        } else {
                            MapUtils.openBrowserToGuide(
                                requireActivity(),
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

        RxPermissions(requireActivity()).request(
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
                        requireActivity(),
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
                                            requireActivity(),
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
                                            requireActivity(),
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
                    ToastUtils.showShortToast(requireContext(), "请同意拍照相关权限")
                }
            }
    }

    private fun uploadImages(imagePath: String?) {
        if (imagePath == null || imagePath.isEmpty()) {
            ToastUtils.showShortToast(requireContext(), "获取图片出错")
            return
        }
        netViewModel.uploadImage(imagePath)
        showLoadingDialog()
    }

    private fun downloadFile(fileUrl: String, path: String) {
        RxPermissions(requireActivity()).request(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
            .subscribe { aBoolean: Boolean ->
                if (aBoolean) {
                    showLoadingDialog("下载中")
                    FileDownloader.setup(requireContext())
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
                                ToastUtils.showShortToast(requireContext(), e?.message)
                                hideLoadingDialog()
                            }

                            override fun completed(task: BaseDownloadTask?) {
                                super.completed(task)
                                ToastUtils.showShortToast(requireContext(), "下载完成")
                                hideLoadingDialog()
                            }
                        }).start()
                } else {
                    ToastUtils.showShortToast(requireContext(), "无存储权限")
                }
            }
    }

    private fun downloadAndLoad(fileName: String, zipUrl: String) {
        if (zipUrl.isEmpty()) {
            ToastUtils.showShortToast(requireContext(), "下载链接错误")
            return
        }
        val filesDir = requireActivity().filesDir
        val zipFile = "$filesDir/$fileName.zip"
        FileDownloader.setup(requireContext())
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
                    ToastUtils.showShortToast(requireContext(), e?.message)
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
                        val sharedPref =
                            requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return
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

            }
            EventBusMessage.MSG_TYPE_APP_REFRESH -> {
                webview?.reload()
            }
        }
    }

    fun reLoad() {
        try {
            webview?.reload()
        } catch (e: Exception) {
        }
    }

    fun reLoadData() {
        try {
            webview?.callHandler(JSConst.JSCONST_UDRELOADDATA, "1") {}
        } catch (e: Exception) {
        }
    }

    fun canBack(): Boolean {
        if (webview == null) return false
        return webview!!.canGoBack()
    }

    fun toBack() {
        webview?.goBack()
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
            ToastUtils.showLongToast(requireContext(), "未设置分享内容")
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
        CookieSyncManager.createInstance(requireActivity())
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
