package com.uniondrug.udlib.web.utils

import android.content.pm.PackageManager
import java.util.*

object PackageManagerUtils {
    private val mPackageNames: MutableList<String> =
        ArrayList()
    private const val GAODE_PACKAGE_NAME = "com.autonavi.minimap"
    private const val BAIDU_PACKAGE_NAME = "com.baidu.BaiduMap"
    private fun initPackageManager(packageManager: PackageManager) {
        val packageInfos =
            packageManager.getInstalledPackages(0)
        if (packageInfos != null) {
            for (i in packageInfos.indices) {
                mPackageNames.add(packageInfos[i].packageName)
            }
        }
    }

    fun haveGaodeMap(packageManager: PackageManager): Boolean {
        initPackageManager(packageManager)
        return mPackageNames.contains(GAODE_PACKAGE_NAME)
    }

    fun haveBaiduMap(packageManager: PackageManager): Boolean {
        initPackageManager(packageManager)
        return mPackageNames.contains(BAIDU_PACKAGE_NAME)
    }
}