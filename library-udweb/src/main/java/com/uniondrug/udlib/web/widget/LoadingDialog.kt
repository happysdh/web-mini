package com.uniondrug.udlib.web.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import com.uniondrug.udlib.web.R
import kotlinx.android.synthetic.main.udweb_dialog_loading.*

class LoadingDialog(context: Context,textValue: String) : Dialog(context, R.style.BaseDialog) {
    private var text = ""
    init {
        text = textValue
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.udweb_dialog_loading)
        setCancelable(false)
        tvLoading.text = text
//        try {
//            findViewById<TextView>(R.id.tvLoading).text = text
//        } catch (e: Exception) {
//        }
    }

}