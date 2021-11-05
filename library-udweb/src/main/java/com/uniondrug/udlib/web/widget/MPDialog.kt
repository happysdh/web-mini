package com.uniondrug.udlib.web.widget

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.uniondrug.udlib.web.R
import kotlinx.android.synthetic.main.udweb_dialog_mp.*

class MPDialog(context: Context, hasShare: Boolean,hasPicShare:Boolean) : BaseAlertDialog(context),
    View.OnClickListener {
    private var share:Boolean = false
    private var mHasPicShare:Boolean = false
    init {
        share = hasShare
        mHasPicShare = hasPicShare
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        setGravity(Gravity.BOTTOM)
        setMarginHorizontal(0)
        setAnimationStyle(R.style.dialog_mp)
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.udweb_dialog_mp, null)
        setContentView(view)
        setCancelable(false)
        tvClose.setOnClickListener(this)
        tvReload.setOnClickListener(this)
        tvShareWeChat.setOnClickListener(this)
        tvShareMoment.setOnClickListener(this)
        tvSharePic.setOnClickListener(this)
        //隐藏分享
        tvShareWeChat.visibility = if(!share)View.GONE else View.VISIBLE
        tvShareMoment.visibility = if(!share)View.GONE else View.VISIBLE
        tvSharePic.visibility = if(!mHasPicShare)View.GONE else View.VISIBLE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvClose -> {
                dismiss()
            }
            R.id.tvReload -> {
                if (dialogClickListener != null) dialogClickListener?.onClickListener(1)
                dismiss()
            }
            R.id.tvShareMoment -> {
                if (dialogClickListener != null) dialogClickListener?.onClickListener(2)
                dismiss()
            }
            R.id.tvShareWeChat -> {
                if (dialogClickListener != null) dialogClickListener?.onClickListener(3)
                dismiss()
            }
            R.id.tvSharePic -> {
                if (dialogClickListener != null) dialogClickListener?.onClickListener(4)
                dismiss()
            }
        }
    }
}