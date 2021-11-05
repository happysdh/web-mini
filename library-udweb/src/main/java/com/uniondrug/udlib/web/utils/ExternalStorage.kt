package com.uniondrug.udlib.web.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.os.StatFs
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.io.IOException

class ExternalStorage private constructor() {
    /**
     * 外部存储根目录
     */
    private var sdkStorageRoot: String? = null
    private var hasPermission = true // 是否拥有存储卡权限
    private var context: Context? = null
    fun init(context: Context, sdkStorageRoot: String) {
        this.context = context
        // 判断权限
        hasPermission = checkPermission(context)
        if (!TextUtils.isEmpty(sdkStorageRoot)) {
            val dir = File(sdkStorageRoot)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            if (dir.exists() && !dir.isFile) {
                this.sdkStorageRoot = sdkStorageRoot
                if (!sdkStorageRoot.endsWith("/")) {
                    this.sdkStorageRoot = "$sdkStorageRoot/"
                }
            }
        }
        if (TextUtils.isEmpty(this.sdkStorageRoot)) {
            loadStorageState(context)
        }
        createSubFolders()
    }

    private fun loadStorageState(context: Context) {
        val externalPath =
            Environment.getExternalStorageDirectory().path
        sdkStorageRoot = externalPath + "/" + context.packageName + "/"
    }

    private fun createSubFolders() {
        var result = true
        val root = File(sdkStorageRoot)
        if (root.exists() && !root.isDirectory) {
            root.delete()
        }
        for (storageType in StorageType.values()) {
            result = result and makeDirectory(sdkStorageRoot + storageType.storagePath)
        }
        if (result) {
            createNoMediaFile(sdkStorageRoot)
        }
    }

    /**
     * 创建目录
     *
     * @param path
     * @return
     */
    private fun makeDirectory(path: String): Boolean {
        val file = File(path)
        var exist = file.exists()
        if (!exist) {
            exist = file.mkdirs()
        }
        return exist
    }

    private fun createNoMediaFile(path: String?) {
        val noMediaFile =
            File("$path/$NO_MEDIA_FILE_NAME")
        try {
            if (!noMediaFile.exists()) {
                noMediaFile.createNewFile()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 文件全名转绝对路径（写）
     *
     * @param fileName 文件全名（文件名.扩展名）
     * @return 返回绝对路径信息
     */
    fun getWritePath(fileName: String, fileType: StorageType): String {
        return pathForName(fileName, fileType, false, false)
    }

    private fun pathForName(
        fileName: String, type: StorageType, dir: Boolean,
        check: Boolean
    ): String {
        val directory = getDirectoryByDirType(type)
        val path = StringBuilder(directory)
        if (!dir) {
            path.append(fileName)
        }
        val pathString = path.toString()
        val file = File(pathString)
        return if (check) {
            if (file.exists()) {
                if (dir && file.isDirectory
                    || !dir && !file.isDirectory
                ) {
                    return pathString
                }
            }
            ""
        } else {
            pathString
        }
    }

    /**
     * 返回指定类型的文件夹路径
     *
     * @param fileType
     * @return
     */
    fun getDirectoryByDirType(fileType: StorageType): String {
        return sdkStorageRoot + fileType.storagePath
    }

    /**
     * 根据输入的文件名和类型，找到该文件的全路径。
     *
     * @param fileName
     * @param fileType
     * @return 如果存在该文件，返回路径，否则返回空
     */
    fun getReadPath(fileName: String, fileType: StorageType): String {
        return if (TextUtils.isEmpty(fileName)) {
            ""
        } else pathForName(fileName, fileType, false, true)
    }

    val isSdkStorageReady: Boolean
        get() {
            val externalRoot =
                Environment.getExternalStorageDirectory().absolutePath
            return if (sdkStorageRoot!!.startsWith(externalRoot)) {
                Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
            } else {
                true
            }
        }

    /**
     * 获取外置存储卡剩余空间
     *
     * @return
     */
    val availableExternalSize: Long
        get() = getResidualSpace(sdkStorageRoot)

    /**
     * 获取目录剩余空间
     *
     * @param directoryPath
     * @return
     */
    private fun getResidualSpace(directoryPath: String?): Long {
        try {
            val sf = StatFs(directoryPath)
            val blockSize = sf.blockSize.toLong()
            val availCount = sf.availableBlocks.toLong()
            return availCount * blockSize
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    /**
     * SD卡存储权限检查
     */
    private fun checkPermission(context: Context?): Boolean {
        if (context == null) {
            Log.e(TAG, "checkMPermission context null")
            return false
        }

        // 写权限有了默认就赋予了读权限
        val pm = context.packageManager
        if (PackageManager.PERMISSION_GRANTED !=
            pm.checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                context.applicationInfo.packageName
            )
        ) {
            Log.e(
                TAG,
                "without permission to access storage"
            )
            return false
        }
        return true
    }

    /**
     * 有效性检查
     */
    fun checkStorageValid(): Boolean {
        if (hasPermission) {
            return true // M以下版本&授权过的M版本不需要检查
        }
        hasPermission = checkPermission(context) // 检查是否已经获取权限了
        if (hasPermission) {
            Log.i(TAG, "get permission to access storage")

            // 已经重新获得权限，那么重新检查一遍初始化过程
            createSubFolders()
        }
        return hasPermission
    }

    companion object {
        @get:Synchronized
        var instance: ExternalStorage? = null
            get() {
                if (field == null) {
                    field = ExternalStorage()
                }
                return field
            }
            private set
        private const val TAG = "ExternalStorage"

        protected var NO_MEDIA_FILE_NAME = ".nomedia"
    }
}