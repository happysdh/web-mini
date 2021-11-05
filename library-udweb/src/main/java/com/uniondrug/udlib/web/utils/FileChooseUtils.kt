package com.uniondrug.udlib.web.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.SystemClock
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class FileChooseUtils {
    companion object {
        fun getFile(context: Context, uri: Uri): FileBean {
            Log.w("TAG", "UploadFile: $uri")
            Log.w("TAG", "UploadFile: authority->" + uri.authority)
            val fileBean = FileBean()
            var path: String? = ""
            if (DocumentsContract.isDocumentUri(context, uri)) {
                Log.w("TAG", "UploadFile: type->1")
                //如果是document类型的Uri，通过document id处理，内部会调用Uri.decode(docId)进行解码
                val docId = DocumentsContract.getDocumentId(uri)
                //primary:Azbtrace.txt
                //video:A1283522
                val splits: List<String> = docId.split(":")
                var type: String = ""
                var id: String = ""
                if (splits.size == 2) {
                    type = splits[0]
                    id = splits[1]
                }
                when (uri.authority) {
                    "com.android.externalstorage.documents" -> {
                        if ("primary" == type) {
                            path =
                                Environment.getExternalStorageDirectory().absolutePath + File.separator + id
                        }
                    }
                    "com.android.providers.downloads.documents" -> {
                        when (type) {
                            "primary" -> {
                                path = Environment.getExternalStorageDirectory()
                                    .toString() + File.separator + id
                            }
                            "raw" -> {
                                path = id
                            }
                            "msf" -> {
                                ToastUtils.showShortToast(context, "暂不支持此路径下文件")
                            }
                        }
                    }
                    "com.android.providers.media.documents" -> {
                        var externalUri: Uri? = null
                        when (type) {
                            "image" -> {
                                externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            }
                            "video" -> {
                                externalUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            }
                            "audio" -> {
                                externalUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            }
                        }
                        if (externalUri != null) {
                            val selection = "_id=?"
                            val selectionArgs = arrayOf(id)
                            path = getMediaPathFromUri(
                                context,
                                externalUri,
                                selection,
                                selectionArgs,
                                fileBean
                            )
                        }
                    }
                }
            } else if (isQQMediaDocument(uri)) {
                Log.w("TAG", "UploadFile: type->2")
                path = try {
                    val fileDir = Environment.getExternalStorageDirectory()
                    val file =
                        File(fileDir, uri.path.substring("/QQBrowser".length, uri.path.length))
                    file.absolutePath
                } catch (e: Exception) {
                    ""
                }
            } else if (uri.scheme.toLowerCase(Locale.ROOT) == ContentResolver.SCHEME_CONTENT) {
                Log.w("TAG", "UploadFile: type->3")
                path = getMediaPathFromUri(context, uri, "", null, fileBean)
            } else if (uri.scheme.toLowerCase(Locale.ROOT) == ContentResolver.SCHEME_FILE) {
                Log.w("TAG", "UploadFile: type->4")
                path = uri.path
            }
            fileBean.path = path
            return fileBean
        }

        private fun getMediaPathFromUri(
            context: Context,
            uri: Uri,
            selection: String,
            selectionArgs: Array<String>?, fileBean: FileBean
        ): String {
            var path = uri.path
            val sdPath: String = Environment.getExternalStorageDirectory().absolutePath
            if (!path.startsWith(sdPath)) {
                val sepIndex = path.indexOf(File.separator, 1);
                path = if (sepIndex == -1) ""
                else {
                    sdPath + path.substring(sepIndex);
                }
            }

            if (!File(path).exists()) {
                val resolver = context.contentResolver
                val projection = arrayOf(MediaStore.MediaColumns.DATA)
                val cursor: Cursor =
                    resolver.query(uri, projection, selection, selectionArgs, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        try {
                            val index = cursor.getColumnIndexOrThrow(projection[0])
                            if (index != -1) path = cursor.getString(index)
                        } catch (e: IllegalArgumentException) {
                            e.printStackTrace()
                            Log.w("TAG", "UploadFile: " + e.message)
                            val fileName = getFileName(uri)
                            val newFileName = addRandomToFileName(fileName)
                            if (!TextUtils.isEmpty(fileName) && !TextUtils.isEmpty(newFileName)) {
                                try {
                                    val dir = File(context.cacheDir.absolutePath)
                                    if (!dir.exists()) {
                                        dir.mkdirs()
                                    }
                                    val file = File(dir, newFileName)
                                    val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                                    val output: OutputStream = FileOutputStream(file)
                                    val buffer = ByteArray(4 * 1024)
                                    var read: Int
                                    while (inputStream.read(buffer).also { read = it } != -1) {
                                        output.write(buffer, 0, read)
                                    }
                                    output.flush()
                                    output.close()
                                    path = file.absolutePath
                                    fileBean.name = fileName!!
                                } catch (e: Exception) {
                                    path = ""
                                }
                            }

                        } finally {
                            cursor.close();
                        }
                    }
                }
            }
            return path
        }

        private fun isQQMediaDocument(uri: Uri): Boolean {
            return "com.tencent.mtt.fileprovider" == uri.authority
        }

        fun getFileName(pathandname: String): String {
            val start = pathandname.lastIndexOf("/")
            val end = pathandname.length
            return if (start != -1 && end != -1) {
                pathandname.substring(start + 1, end)
            } else {
                "";
            }
        }

        private fun getFileName(uri: Uri?): String? {
            if (uri == null) return null
            var fileName: String? = null
            val path = uri.path
            val cut = path.lastIndexOf('/')
            if (cut != -1) {
                fileName = path.substring(cut + 1)
            }
            return fileName
        }

        private fun addRandomToFileName(fileName: String?): String {
            if (fileName == null) return ""
            return try {
                val start = fileName.lastIndexOf(".")
                fileName.substring(0, start) + "_" + SystemClock.uptimeMillis()
                    .toString() + fileName.substring(start)
            } catch (e: Exception) {
                ""
            }
        }
    }
}