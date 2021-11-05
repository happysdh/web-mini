package com.uniondrug.udlib.web.activity

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.uniondrug.udlib.web.common.Extras
import com.uniondrug.udlib.web.R
import com.uniondrug.udlib.web.utils.ToastUtils
import java.io.File

class PickImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.udweb_activity_pick_image)
    }

    private val IMAGE_TYPE = "image/*"
    private val KEY_STATE = "state"

    private val REQUEST_CODE_LOCAL = Extras.FROM_LOCAL
    private val REQUEST_CODE_CAMERA = Extras.FROM_CAMERA
    private val REQUEST_CODE_CROP = Extras.FROM_CROP
    private var inited = false

    //  1:不裁剪
    //  2：正方形
    //  3：3比2
    private var cropType = "1"

    override fun onResume() {
        super.onResume()
        if (!inited) {
            processIntent()
            inited = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_STATE, inited)
        cropType = try {
            intent.getStringExtra("cropType")
        } catch (e: Exception) {
            "1"
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState != null) {
            inited = savedInstanceState.getBoolean(KEY_STATE)
        }
    }

    private fun processIntent() {
        val from = intent.getIntExtra(Extras.EXTRA_FROM, Extras.FROM_LOCAL)
        if (from == Extras.FROM_LOCAL) {
            pickFromLocal()
        } else {
            pickFromCamera()
        }
    }

    private fun pickFromLocal() {
        val intent = pickIntent()
        if (intent == null) {
            finish()
            return
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_LOCAL)
        } catch (e: ActivityNotFoundException) {
            ToastUtils.showShortToast(application, "你的手机没有图库程序")
            finish()
        } catch (e: Exception) {
            ToastUtils.showShortToast(application, e.message)
            finish()
        }
    }

    private fun pickFromCamera() {
        try {
            val outPath = intent.getStringExtra(Extras.EXTRA_FILE_PATH)
            if (TextUtils.isEmpty(outPath)) {
                ToastUtils.showShortToast(application, "存储空间不足，无法保存")
                finish()
                return
            }
            val outputFile = File(outPath)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile))
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile))
            } else {
                val contentValues = ContentValues(1)
                contentValues.put(MediaStore.Images.Media.DATA, outputFile.absolutePath)
                val uri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            }
            startActivityForResult(intent, REQUEST_CODE_CAMERA)
        } catch (e: ActivityNotFoundException) {
            finish()
        } catch (e: Exception) {
            ToastUtils.showShortToast(application, e.message)
            finish()
        }
    }

    private fun pickIntent(): Intent? {
        val isSupportOrg = intent.getBooleanExtra(Extras.EXTRA_SUPPORT_ORIGINAL, false)
        val enterIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        enterIntent.putExtra(Extras.EXTRA_SUPPORT_ORIGINAL, isSupportOrg)
        return enterIntent
    }

    private fun pathFromResult(data: Intent?): String? {
        var outPath = intent.getStringExtra(Extras.EXTRA_FILE_PATH)
        if (data == null || data.data == null) {
            return outPath
        }
        val uri = data.data
        val cursor = contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
        return if (cursor == null) {
            // miui 2.3 有可能为null
            uri.path
        } else {
            if (uri.toString()
                    .contains("content://com.android.providers.media.documents/document/image")
            ) {
                // 获取图片地址
                var _id: String?
                val urlencoded = Uri.decode(uri.toString())
                val id_index = urlencoded.lastIndexOf(":")
                _id = urlencoded.substring(id_index + 1)
                val mauro = contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null,
                    " _id = $_id",
                    null,
                    null
                )
                mauro.moveToFirst()
                val column_index = mauro.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                outPath = mauro.getString(column_index)
                if (!mauro.isClosed) {
                    mauro.close()
                }
            } else {
                cursor.moveToFirst()
                val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                outPath = cursor.getString(column_index)
            }
            if (!cursor.isClosed) {
                cursor.close()
            }
            outPath
        }
    }

    private fun onPickedLocal(data: Intent?) {
        try {
            if (data != null) {
                val selectedImage = data.data
                val filePathColumn =
                    arrayOf(MediaStore.Images.Media.DATA)
                val cursor =
                    contentResolver.query(selectedImage, filePathColumn, null, null, null)
                cursor.moveToFirst()
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                val photoPath = cursor.getString(columnIndex)
                cursor.close()
                if (cropType == "1") {
                    val result = Intent()
                    result.putExtra(Extras.EXTRA_FROM_LOCAL, true)
                    result.putExtra(Extras.EXTRA_FILE_PATH, photoPath)
                    setResult(RESULT_OK, result)
                    finish()
                } else {
                    val intent = Intent(this, CropImageActivity::class.java)
                    intent.putExtra(Extras.EXTRA_FILE_PATH, pathFromResult(data))
                    intent.putExtra("isSquare", cropType == "2")
                    startActivityForResult(intent, REQUEST_CODE_CROP)
                }
            } else {
                finish()
            }
        } catch (e: Exception) {
            ToastUtils.showShortToast(application, "获取图片出错")
            finish()
        }
    }

    private fun onPickedCamera(data: Intent?, code: Int) {
        try {
            val photoPath = pathFromResult(data)
            if (!TextUtils.isEmpty(photoPath)) {
                if (cropType == "1") {
                    val result = Intent()
                    result.putExtra(Extras.EXTRA_FROM_LOCAL, code == REQUEST_CODE_LOCAL)
                    result.putExtra(Extras.EXTRA_FILE_PATH, photoPath)
                    setResult(RESULT_OK, result)
                    finish()
                } else {
                    val intent = Intent(
                        this,
                        CropImageActivity::class.java
                    )
                    intent.putExtra(Extras.EXTRA_FILE_PATH, pathFromResult(data))
                    intent.putExtra("isSquare", cropType == "2")
                    startActivityForResult(intent, REQUEST_CODE_CROP)
                }
            } else {
                finish()
            }
        } catch (e: Exception) {
            ToastUtils.showShortToast(application, "获取图片出错")
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            finish()
            return
        }
        when (requestCode) {
            REQUEST_CODE_LOCAL -> onPickedLocal(data)
            REQUEST_CODE_CAMERA -> onPickedCamera(data, requestCode)
            REQUEST_CODE_CROP -> {
                val result = Intent()
                result.putExtra(
                    Extras.EXTRA_FILE_PATH, data?.getStringExtra(
                        Extras.EXTRA_FILE_PATH
                    )
                )
                setResult(RESULT_OK, result)
                finish()
            }
            else -> {
            }
        }
    }
}
