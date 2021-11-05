package com.uniondrug.udlib.web.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.lzyzsd.jsbridge.CallBackFunction
import com.uniondrug.udlib.web.widget.MultiChoosePopUp
import kotlinx.android.synthetic.main.udweb_activity_ud_web_view.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object UDSDKUtils {
    fun saveImage(context: Activity, data: String) {
        try {
            val bitmap: Bitmap? = webData2bitmap(data)
            if (bitmap != null) {
                save2Album(
                    context,
                    bitmap,
                    SimpleDateFormat("SXS_yyyyMMddHHmmss", Locale.getDefault()).format(Date())
                        .toString() + ".jpg"
                )
            } else {
                context.runOnUiThread {
                    Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT)
                }
            }
        } catch (e: java.lang.Exception) {
            context.runOnUiThread {
                Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT)
            }
            e.printStackTrace()
        }
    }


    private fun webData2bitmap(data: String): Bitmap? {
        val imageBytes: ByteArray =
            Base64.decode(data.split(",".toRegex()).toTypedArray()[1], Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun save2Album(context: Activity, bitmap: Bitmap, fileName: String) {
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
            context.runOnUiThread {
                context.sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(file)
                    )
                )
                Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT)
            }
        } catch (e: java.lang.Exception) {
            context.runOnUiThread {
                Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT)
            }
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (ignored: java.lang.Exception) {
            }
        }
    }

    fun toNavigate(
        activity: Activity,
        packageManager: PackageManager,
        latitude: Double,
        longitude: Double,
        address: String,
        parent: View
    ) {
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
        MultiChoosePopUp(activity, list, "请选择地图",
            object : MultiChoosePopUp.ItemClickCallback {
                override fun onClick(position: Int) {
                    if (list[position] == "百度地图") {
                        MapUtils.openBaiduMapToGuide(
                            activity, latitude,
                            longitude, address
                        )
                    } else if (list[position] == "高德地图") {
                        if (hasGaode) {
                            MapUtils.openGaodeMapToGuide(
                                activity,
                                latitude.toString(),
                                longitude.toString(),
                                address
                            )
                        } else {
                            MapUtils.openBrowserToGuide(
                                activity,
                                latitude.toString(),
                                longitude.toString(),
                                address
                            )
                        }
                    }
                }
            }
        ).showAtLocation(parent, Gravity.CENTER, 0, 0)
    }

    fun sendJSON2JS(mCallBacks: MutableMap<String, CallBackFunction>,event: String, success: Boolean, obj: JSONObject) {
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

}