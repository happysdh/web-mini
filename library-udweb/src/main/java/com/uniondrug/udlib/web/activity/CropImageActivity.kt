package com.uniondrug.udlib.web.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.uniondrug.udlib.web.common.Extras
import com.uniondrug.udlib.web.R
import com.uniondrug.udlib.web.utils.FileUtils
import kotlinx.android.synthetic.main.udweb_activity_crop_image.*
import java.io.File

class CropImageActivity : AppCompatActivity(), View.OnClickListener {
    private var mOutputX = 0
    private var mOutputY = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.udweb_activity_crop_image)
        val path = intent?.extras?.getString(Extras.EXTRA_FILE_PATH, "")
        val isSquare = intent?.extras?.getBoolean("isSquare", true)
        try {
            cropImageView.setImageURI(Uri.fromFile(File(path)))
        } catch (e: Exception) {
        }
        //获取需要的参数
        mOutputX = 800
        mOutputY = if (isSquare!!) 800 else 533
        cropImageView.setIsSquare(isSquare)
        tvCancel.setOnClickListener(this)
        tvEnsure.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvCancel -> {
                finish()
            }
            R.id.tvEnsure -> {
                val croppedImage: Bitmap? = cropImageView.getCropBitmap(mOutputX, mOutputY, true)
                val path = FileUtils.saveImage(croppedImage)
                val result = Intent()
                result.putExtra(Extras.EXTRA_FILE_PATH, path)
                setResult(RESULT_OK, result)
                finish()
            }
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
//        setResult(RESULT_CANCELED)
    }
}