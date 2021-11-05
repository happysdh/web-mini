package com.uniondrug.udlib.web.widget

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.uniondrug.udlib.web.R
import kotlinx.android.synthetic.main.udweb_dialog_wx_share.*

class WXShareDialog(context: Context) : BaseAlertDialog(context), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        setGravity(Gravity.BOTTOM)
        setMarginHorizontal(0)
        setAnimationStyle(R.style.dialog_mp)
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.udweb_dialog_wx_share, null)
        setContentView(view)
        setCancelable(true)
        llLink.setOnClickListener(this)
        llWx.setOnClickListener(this)
        llWxCircle.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.llLink -> {
                if (dialogClickListener != null) dialogClickListener?.onClickListener(1)
                dismiss()
            }
            R.id.llWx -> {
                if (dialogClickListener != null) dialogClickListener?.onClickListener(2)
                dismiss()
            }
            R.id.llWxCircle -> {
                if (dialogClickListener != null) dialogClickListener?.onClickListener(3)
                dismiss()
            }
        }
    }
}