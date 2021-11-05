package com.uniondrug.udlib.web.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.uniondrug.udlib.web.utils.GPSUtils.gcj02_To_Bd09

object MapUtils {
    fun openGaodeMapToGuide(
        activity: Activity,
        lat: String,
        lon: String,
        name: String
    ) {
        //t = 0（驾车）= 1（公交）= 2（步行）= 3（骑行）= 4（火车）= 5（长途客车）
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        //&slat="+currLocationX+"&slon="+currLocationY
        val url = ("androidamap://route?sourceApplication=amap"
                + "&dlat=" + lat + "&dlon=" + lon + "&dname=" + name + "&dev=0&t=3")
        val uri = Uri.parse(url)
        //将功能Scheme以URI的方式传入data
        intent.data = uri
        //启动该页面即可
        activity.startActivity(intent)
    }

    fun openBaiduMapToGuide(
        activity: Activity,
        lat: Double,
        lon: Double,
        name: String
    ) {
        //mode: transit、driving、navigation、walking，riding分别表示公交、驾车、导航、步行和骑行
        val intent = Intent()
        val location = gcj02_To_Bd09(lat, lon)
        val url = "baidumap://map/direction?" +
                "destination=name:" + name + "|latlng:" + location[0] + "," + location[1] +
                "&mode=riding&sy=3&index=0&target=1"
        val uri = Uri.parse(url)
        //将功能Scheme以URI的方式传入data
        intent.data = uri
        //启动该页面即可
        activity.startActivity(intent)
    }

    fun openBrowserToGuide(
        activity: Activity,
        lat: String,
        lon: String,
        name: String
    ) {
        //驾车：mode=car
        //公交：mode=bus
        //步行：mode=walk
        //骑行：mode=ride
        val url = "http://uri.amap.com/navigation?to=" + lat + "," + lon + "," +
                name + "&mode=ride&policy=1&src=mypage&coordinate=gaode&callnative=0"
        val uri = Uri.parse(url)
        val intent =
            Intent(Intent.ACTION_VIEW, uri)
        activity.startActivity(intent)
    }
}