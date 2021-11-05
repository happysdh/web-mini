package com.uniondrug.udlib.web.utils

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView

class AndroidBug5497Workaround private constructor(
    activity: Activity,
    webView: WebView
) {
    private val mChildOfContent: View
    private var usableHeightPrevious = 0
    private val frameLayoutParams: ViewGroup.LayoutParams
    private fun possiblyResizeChildOfContent(activity: Activity) {
        val usableHeightNow = computeUsableHeight(activity)
        if (usableHeightNow != usableHeightPrevious) {
            val usableHeightSansKeyboard =
                mChildOfContent.rootView.measuredHeight - getNavigationBarHeight(activity)
            //            int usableHeightSansKeyboard = activity.getWindowManager().getDefaultDisplay().getHeight();
            val heightDifference = usableHeightSansKeyboard - usableHeightNow
            if (heightDifference > usableHeightSansKeyboard / 4) {
                // keyboard probably just became visible
                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference
            } else {
                // keyboard probably just became hidden
                frameLayoutParams.height = usableHeightSansKeyboard
            }
            mChildOfContent.requestLayout()
            usableHeightPrevious = usableHeightNow
        }
    }

    private fun computeUsableHeight(activity: Activity): Int {
        val r = Rect()
        mChildOfContent.getWindowVisibleDisplayFrame(r)
        return r.bottom - getNavigationBarHeight(activity)
        //        return (r.bottom - r.top);
    }

    private fun getNavigationBarHeight(activity: Activity): Int {
        val resources = activity.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        var height = 0
        if (isNavigationBarExist(activity) && resourceId > 0) {
            height = resources.getDimensionPixelSize(resourceId)
        }
        return height
    }

    companion object {
        fun assistActivity(
            activity: Activity,
            view: WebView
        ) {
            AndroidBug5497Workaround(activity, view)
        }

        private const val NAVIGATION = "navigationBarBackground"

        // 该方法需要在View完全被绘制出来之后调用，否则判断不了
        //在比如 onWindowFocusChanged（）方法中可以得到正确的结果
        private fun isNavigationBarExist(activity: Activity): Boolean {
            val vp =
                activity.window.decorView as ViewGroup
            for (i in 0 until vp.childCount) {
                vp.getChildAt(i).context.packageName
                if (vp.getChildAt(i)
                        .id != View.NO_ID && NAVIGATION == activity.resources
                        .getResourceEntryName(vp.getChildAt(i).id)
                ) {
                    return true
                }
            }
            return false
        }
    }

    init {
        mChildOfContent = webView
        mChildOfContent.viewTreeObserver
            .addOnGlobalLayoutListener {
                possiblyResizeChildOfContent(activity)
            }
        frameLayoutParams = mChildOfContent.getLayoutParams()
    }
}