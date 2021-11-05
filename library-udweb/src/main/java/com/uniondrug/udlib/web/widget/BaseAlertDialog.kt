package com.uniondrug.udlib.web.widget

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import com.uniondrug.udlib.web.R
import com.uniondrug.udlib.web.utils.DensityUtils.Companion.dp2px
import com.uniondrug.udlib.web.utils.ScreenUtils.Companion.getScreenWidth
import io.reactivex.disposables.Disposable

open class BaseAlertDialog protected constructor(context: Context) : AlertDialog(context) {
    protected var dialogClickListener: DialogClickListener? = null
    protected var autoDismissListener: AutoDismissListener? = null
    protected var dialogButtonListener: DialogButtonClickListener? = null
    private var countDown: Disposable? = null
    private var mGravity: Int = Gravity.CENTER
    private var mAnimationStyle: Int = -1
    private var marginHorizontal: Float = 28 * 2.toFloat()

    interface DialogClickListener {
        fun onClickListener(position: Int)
    }

    interface DialogButtonClickListener {
        fun onClickListener(`object`: Any)
    }

    interface AutoDismissListener {
        fun onAutoDismissListener()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = window
        if (window != null) {
            try {
                val lp = window.attributes
                //不设置宽度就是MATCH_PARENT
                lp.width = getScreenWidth(context) - dp2px(context, marginHorizontal)
                window.attributes = lp
                window.setGravity(mGravity)
                //若有圆弧边框 必须得加上透明背景
                window.setBackgroundDrawableResource(R.color.transparent)
                if (mAnimationStyle != -1) window.setWindowAnimations(mAnimationStyle)
            } catch (e: Exception) {
            }
        }
        setOnDismissListener { dialog: DialogInterface? ->
            if (countDown != null) {
                countDown!!.dispose()
            }
        }
    }

    fun setDialogClickListener(listener: DialogClickListener): BaseAlertDialog {
        dialogClickListener = listener
        return this
    }

    fun setGravity(gravity: Int): BaseAlertDialog {
        mGravity = gravity
        return this
    }

    fun setMarginHorizontal(marginDp: Int): BaseAlertDialog {
        marginHorizontal = marginDp.toFloat()
        return this
    }

    fun setAnimationStyle(style: Int): BaseAlertDialog {
        mAnimationStyle = style
        return this
    }

//    fun setAutoDismissSeconds(seconds: Int, listener: AutoDismissListener): BaseAlertDialog {
//        autoDismissListener = listener
//        countDown = Observable.interval(0, 1, TimeUnit.SECONDS)
//            .take(seconds.toLong())
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { aLong: Long ->
//                if (aLong == seconds - 1.toLong() && autoDismissListener != null) {
//                    autoDismissListener!!.onAutoDismissListener()
//                    dismiss()
//                }
//            }
//        return this
//    }
}