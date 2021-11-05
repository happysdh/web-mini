package com.uniondrug.udlib.web.utils

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import java.io.File

object StorageUtils {
    const val K: Long = 1024
    const val M = 1024 * 1024.toLong()

    // 外置存储卡默认预警临界值
    private const val THRESHOLD_WARNING_SPACE = 100 * M

    // 保存文件时所需的最小空间的默认值
    const val THRESHOLD_MIN_SPCAE = 20 * M
    fun init(context: Context, rootPath: String) {
        ExternalStorage.instance!!.init(context, rootPath)
    }

    /**
     * 获取文件保存路径，没有toast提示
     *
     * @param fileName
     * @param fileType
     * @return 可用的保存路径或者null
     */
    fun getWritePath(fileName: String, fileType: StorageType): String? {
        return getWritePath(null, fileName, fileType, false)
    }

    /**
     * 获取文件保存路径
     *
     * @param fileName
     * 文件全名
     * @param tip
     * 空间不足时是否给出默认的toast提示
     * @return 可用的保存路径或者null
     */
    private fun getWritePath(
        context: Context?,
        fileName: String,
        fileType: StorageType,
        tip: Boolean
    ): String? {
        val path: String = ExternalStorage.instance!!.getWritePath(fileName, fileType)
        if (TextUtils.isEmpty(path)) {
            return null
        }
        val dir = File(path).parentFile
        if (dir != null && !dir.exists()) {
            dir.mkdirs()
        }
        return path
    }

    /**
     * 判断能否使用外置存储
     */
    val isExternalStorageExist: Boolean
        get() = ExternalStorage.instance!!.isSdkStorageReady

    /**
     * 判断外部存储是否存在，以及是否有足够空间保存指定类型的文件
     *
     * @param context
     * @param fileType
     * @param tip  是否需要toast提示
     * @return false: 无存储卡或无空间可写, true: 表示ok
     */
    fun hasEnoughSpaceForWrite(
        context: Context?,
        fileType: StorageType,
        tip: Boolean
    ): Boolean {
        if (!ExternalStorage.instance!!.isSdkStorageReady) {
            return false
        }
        val residual: Long = ExternalStorage.instance!!.availableExternalSize
        if (residual < fileType.storageMinSize) {
            return false
        } else if (residual < THRESHOLD_WARNING_SPACE) {
        }
        return true
    }

    /**
     * 根据输入的文件名和类型，找到该文件的全路径。
     *
     * @param fileName
     * @param fileType
     * @return 如果存在该文件，返回路径，否则返回空
     */
    fun getReadPath(fileName: String, fileType: StorageType): String {
        return ExternalStorage.instance!!.getReadPath(fileName, fileType)
    }

    /**
     * 获取文件保存路径，空间不足时有toast提示
     *
     * @param context
     * @param fileName
     * @param fileType
     * @return 可用的保存路径或者null
     */
    fun getWritePath(
        context: Context?,
        fileName: String,
        fileType: StorageType
    ): String? {
        return getWritePath(context, fileName, fileType, true)
    }

    fun getDirectoryByDirType(fileType: StorageType): String {
        return ExternalStorage.instance!!.getDirectoryByDirType(fileType)
    }

    val systemImagePath: String
        get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            .absolutePath + "/nim/"

    fun isInvalidVideoFile(filePath: String): Boolean {
        return (filePath.toLowerCase().endsWith(".3gp")
                || filePath.toLowerCase().endsWith(".mp4"))
    }
}