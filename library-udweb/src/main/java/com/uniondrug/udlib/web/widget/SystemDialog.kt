package com.uniondrug.udlib.web.widget

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.uniondrug.udlib.web.R

class SystemDialog(context: Context) : BaseAlertDialog(context),
    View.OnClickListener {
    var title: String? = null
    var left: String? = null
    var right: String? = null
    var content: String? = null
    var headImgResId = 0
    var leftColor = 0
    var leftVisible = true
    fun setHeadImg(resId: Int): SystemDialog {
        headImgResId = resId
        return this
    }

    fun setTitle(title: String?): SystemDialog {
        this.title = title
        return this
    }

    fun setContent(content: String?): SystemDialog {
        this.content = content
        return this
    }

    fun setLeft(left: String?): SystemDialog {
        this.left = left
        return this
    }

    fun setLeft(left: String?, color: Int): SystemDialog {
        this.left = left
        leftColor = color
        return this
    }

    fun setRight(right: String?): SystemDialog {
        this.right = right
        return this
    }

    fun setLeftVisible(leftVisible: Boolean): SystemDialog {
        this.leftVisible = leftVisible
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context)
            .inflate(R.layout.udweb_dialog_system, null)
        //        systemBinding = DataBindingUtil.bind(view);
//        systemBinding.setListener(this);
//        if (title != null) systemBinding.tvHead.setText(title);
//        if (content != null) systemBinding.tvContent.setText(content);
//        if (left != null) systemBinding.tvLeft.setText(left);
//        if (right != null) systemBinding.tvRight.setText(right);
//        if (headImgResId != 0) {
//            systemBinding.imgHead.setImageResource(headImgResId);
//            systemBinding.imgHead.setVisibility(View.VISIBLE);
//        }
//        if (leftColor != 0) systemBinding.tvLeft.setTextColor(leftColor);
//        systemBinding.tvLeft.setVisibility(leftVisible?View.VISIBLE:View.GONE);
//        systemBinding.viewMid.setVisibility(leftVisible?View.VISIBLE:View.GONE);
        setContentView(view)
        setCancelable(false)
    }

    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.tvLeft) {
            if (dialogClickListener != null) dialogClickListener!!.onClickListener(0)
            dismiss()
        } else if (id == R.id.tvRight) {
            if (dialogClickListener != null) dialogClickListener!!.onClickListener(1)
            dismiss()
        }
    }
}