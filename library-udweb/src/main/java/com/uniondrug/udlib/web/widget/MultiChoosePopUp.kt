package com.uniondrug.udlib.web.widget

import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.uniondrug.udlib.web.R

class MultiChoosePopUp(
    private val activity: Activity,
    private val mStringList: List<String>,
    title: String?,
    private val mItemClickCallback: ItemClickCallback
) : PopupWindow(activity) {
    private var tv1: TextView? = null

    interface ItemClickCallback {
        fun onClick(position: Int)
    }

    init {
        contentView = LayoutInflater.from(activity).inflate(R.layout.udweb_dialog_multi_choose, null)
        val baseAdapter = object : BaseAdapter() {
            override fun getCount(): Int {
                return mStringList.size
            }

            override fun getItem(position: Int): Any? {
                return null
            }

            override fun getItemId(position: Int): Long {
                return 0
            }

            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: View = convertView
                    ?: LayoutInflater.from(activity)
                        .inflate(R.layout.udweb_cell_simple_list, parent, false)
                tv1 = view.findViewById<View>(R.id.text1) as TextView
                tv1!!.text = mStringList[position]
                return view
            }
        }
        isFocusable = true // 取得焦点
        //        //注意  要是点击外部空白处弹框消息  那么必须给弹框设置一个背景色  不然是不起作用的
//        Bitmap bitmap = Bitmap.createBitmap(ScreenUtil.getScreenWidth(context), ScreenUtil.getScreenHeight(context),
//                Bitmap.Config.ARGB_8888);
//        bitmap.eraseColor(context.getResources().getColor(R.color.colorLightGray));
        setBackgroundDrawable(BitmapDrawable())
        //点击外部消失
        isOutsideTouchable = false
        //设置可以点击
        isTouchable = true
        //进入退出的动画
//        setAnimationStyle(R.style.message_dialogAnim);

        //设置宽度
        width = ViewGroup.LayoutParams.MATCH_PARENT
        //设置高度
        height = ViewGroup.LayoutParams.MATCH_PARENT
        contentView.findViewById<TextView>(R.id.tvClose).setOnClickListener {
            dismiss()
        }
        val listView = contentView.findViewById<ListView>(R.id.listView)
        listView.adapter = baseAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            mItemClickCallback.onClick(position)
            dismiss()
        }
        //全屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val mLayoutInScreen =
                    PopupWindow::class.java.getDeclaredField("mLayoutInScreen")
                mLayoutInScreen.isAccessible = true
                mLayoutInScreen[this] = true
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}