package com.uniondrug.udlib.web.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.uniondrug.udlib.web.R

object AlexStatusBarUtils {
    const val IS_SET_PADDING_KEY = 54648632
    private val STATUS_VIEW_ID = R.id.status_view
    private val TRANSLUCENT_VIEW_ID = R.id.translucent_view
    //------------单色明暗度状态栏------------
    /**
     * 设置普通toolbar中状态栏颜色以及明暗度
     *
     * @param activity
     * @param color
     * @param statusBarAlpha
     */
    private fun setStatusColor(
        activity: Activity,
        @ColorInt color: Int,
        statusBarAlpha: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window
                .clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = statusColorIntensity(color, statusBarAlpha)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            setStatusViewToAct(activity, color, statusBarAlpha)
            setRootView(activity)
        }
    }

    /**
     * 设置普通toolbar中状态栏颜色
     *
     * @param activity
     * @param color
     */
    fun setStatusColor(activity: Activity, @ColorInt color: Int) {
        setStatusColor(activity, color, 0)
    }

    /**
     * 设置toolbar带drawerLayout状态栏透明度,5.0以上使用默认系统的第二颜色colorPrimaryDark
     * 但是drawerLayout打开的时候会有一条statusbar高度的半透明条
     * 如果想修改，请到style中设置<item name="colorPrimaryDark">@color/colorPrimary</item>
     * 注：必须将drawerLayout设置android:fitsSystemWindows="true"
     * 还有下方的toolbar，
     * 一般不要设置滑动toolbar,在4.4系统会有问题，下部如果有tablayout会遮挡
     *
     * @param activity
     * @param statusBarAlpha
     */
    fun setDyeDrawerStatusAlpha(activity: Activity, statusBarAlpha: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window
                .clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.argb(statusBarAlpha, 0, 0, 0)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            val contentLayout =
                activity.findViewById<View>(android.R.id.content) as ViewGroup
            contentLayout.getChildAt(0).fitsSystemWindows = false
            setTranslucentStatusViewToAct(activity, statusBarAlpha)
        }
    }

    /**
     * toolbar可伸缩版本
     * 设置toolbar带drawerLayout状态栏透明,5.0以上使用默认系统的第二颜色colorPrimaryDark
     * 如果想修改，请到style中设置<item name="colorPrimaryDark">@color/colorPrimary</item>
     * 4.4版本跟随toolbar颜色
     * 但是drawerLayout打开的时候会有一条statusbar高度的半透明条
     * 注：必须将drawerLayout设置android:fitsSystemWindows="true"
     * CoordinatorLayout设置背景颜色，因为4.4状态栏的颜色会跟着它走
     * 下边内容布局设置背景颜色
     *
     * @param activity
     */
    fun setDyeDrawerStatusTransparent(
        activity: Activity,
        coordinatorLayout: CoordinatorLayout
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window
                .clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            val contentLayout =
                activity.findViewById<View>(android.R.id.content) as ViewGroup
            contentLayout.getChildAt(0).fitsSystemWindows = false
            coordinatorLayout.fitsSystemWindows = true
            var mStatusBarView = contentLayout.getChildAt(0)
            //改变颜色时避免重复添加statusBarView
            if (mStatusBarView != null && mStatusBarView.measuredHeight == getStatusBarHeight(
                    activity
                )
            ) {
                return
            }
            mStatusBarView = View(activity)
            val lp = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(activity)
            )
            contentLayout.addView(mStatusBarView, lp)
        }
    }

    /**
     * 颜色不要用0x000000模式的，要用Color.rgb/argb(),或者activity.getResource().getColor等等
     * 设置普通toolbar带drawerLayout状态栏
     * 1.设置toolbar的颜色和颜色光暗度
     * 2.drawerLayout的顶部透明度
     * ,不要把toolbar设置为可滑动
     * 注：必须将drawerLayout设置android:fitsSystemWindows="true"
     *
     * @param activity
     * @param color
     * @param statusBarAlpha
     */
    fun setDyeDrawerStatusColor(
        activity: Activity,
        drawerLayout: DrawerLayout,
        @ColorInt color: Int,
        statusBarAlpha: Int
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window
                .clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.TRANSPARENT
        } else {
            activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        // 生成一个状态栏大小的矩形
        // 添加 statusBarView 到布局中
        val contentLayout =
            drawerLayout.getChildAt(0) as ViewGroup
        val statusBarView =
            contentLayout.findViewById<View>(STATUS_VIEW_ID)
        if (statusBarView != null) {
            if (statusBarView.visibility == View.GONE) {
                statusBarView.visibility = View.VISIBLE
            }
            statusBarView.setBackgroundColor(color)
        } else {
            contentLayout.addView(createStatusBarView(activity, color, 0), 0)
        }
        // 内容布局不是 LinearLayout 时,设置padding top
        if (contentLayout !is LinearLayout && contentLayout.getChildAt(1) != null) {
            contentLayout.getChildAt(1)
                .setPadding(
                    contentLayout.paddingLeft,
                    getStatusBarHeight(activity) + contentLayout.paddingTop,
                    contentLayout.paddingRight,
                    contentLayout.paddingBottom
                )
        }
        // 设置属性
        setDrawerLayoutProperty(drawerLayout, contentLayout)
        setTranslucentStatusViewToAct(activity, statusBarAlpha)
    }

    /**
     * 隐藏statusView
     *
     * @param activity
     */
    fun hideStatusView(activity: Activity) {
        val decorView =
            activity.window.decorView as ViewGroup
        val fakeStatusBarView =
            decorView.findViewById<View>(STATUS_VIEW_ID)
        if (fakeStatusBarView != null) {
            fakeStatusBarView.visibility = View.GONE
        }
        val fakeTranslucentView =
            decorView.findViewById<View>(TRANSLUCENT_VIEW_ID)
        if (fakeTranslucentView != null) {
            fakeTranslucentView.visibility = View.GONE
        }
    }
    //----------透明状态栏，可调整透明度-------------
    /**
     * 设置真正的状态栏透明度
     *
     * @param activity
     * @param statusBarAlpha
     */
    fun setStatusAlpha(activity: Activity, statusBarAlpha: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window
                .clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.argb(statusBarAlpha, 0, 0, 0)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            setTranslucentStatusViewToAct(activity, statusBarAlpha)
            setRootView(activity)
        }
    }

    /**
     * 设置ImageView为第一控件的全透明状态栏
     *
     * @param activity
     */
    fun setTransparentStatusBar(
        activity: Activity,
        topView: View?
    ) {
        setTranslucentStatusBar(activity, topView, 0)
    }

    /**
     * 设置ImageView为第一控件的可以调整透明度的状态栏
     *
     * @param activity
     */
    fun setTranslucentStatusBar(
        activity: Activity,
        topView: View?,
        alpha: Int
    ) {
        setARGBStatusBar(activity, topView, 0, 0, 0, alpha)
    }

    /**
     * 设置透明状态栏版本的状态栏的ARGB
     * @param activity
     * @param topView
     * @param r
     * @param g
     * @param b
     * @param alpha
     */
    fun setARGBStatusBar(
        activity: Activity,
        topView: View?,
        r: Int,
        g: Int,
        b: Int,
        alpha: Int
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window
                .clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.argb(alpha, r, g, b)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            setARGBStatusViewToAct(activity, r, g, b, alpha)
        }
        //        if (topView != null) {
//            boolean isSetPadding = topView.getTag(IS_SET_PADDING_KEY) != null;
//            if (!isSetPadding) {
//                topView.setPadding(topView.getPaddingLeft(), topView.getPaddingTop() + getStatusBarHeight(activity), topView.getPaddingRight(), topView.getPaddingBottom());
//                topView.setTag(IS_SET_PADDING_KEY, true);
//            }
//        }
    }

    /**
     * drawerlayout中设置全透明状态栏
     *
     * @param activity
     * @param drawerLayout
     * @param topView
     */
    fun setTransparentStatusForDrawer(
        activity: Activity,
        drawerLayout: DrawerLayout,
        topView: View?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            drawerLayout.fitsSystemWindows = true
            drawerLayout.clipToPadding = false
        }
        setTransparentStatusBar(activity, topView)
    }

    /**
     * drawerlayout中设置透明状态栏的透明度
     * drawer布局和主布局都会看到
     *
     * @param activity
     * @param drawerLayout
     * @param topView
     * @param statusBarAlpha
     */
    fun setStatusAlphaForDrawer(
        activity: Activity,
        drawerLayout: DrawerLayout,
        topView: View?,
        statusBarAlpha: Int
    ) {
        setStatusColorAndCAlphaForDrawer(
            activity,
            drawerLayout,
            topView,
            0x000000,
            statusBarAlpha
        )
    }

    /**
     * drawerlayout中设置透明状态栏的颜色和透明度
     * drawer布局和主布局都会看到
     *
     * @param activity
     * @param drawerLayout
     * @param topView
     * @param color
     * @param statusBarAlpha
     */
    fun setStatusColorAndCAlphaForDrawer(
        activity: Activity,
        drawerLayout: DrawerLayout,
        topView: View?,
        @ColorInt color: Int,
        statusBarAlpha: Int
    ) {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        setStatusARGBForDrawer(
            activity,
            drawerLayout,
            topView,
            r,
            g,
            b,
            statusBarAlpha
        )
    }

    fun setStatusARGBForDrawer(
        activity: Activity,
        drawerLayout: DrawerLayout,
        topView: View?,
        r: Int,
        g: Int,
        b: Int,
        statusBarAlpha: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            drawerLayout.fitsSystemWindows = true
            drawerLayout.clipToPadding = false
        }
        setARGBStatusBar(activity, topView, r, g, b, statusBarAlpha)
    }

    /**
     * 在有fragment的activity中使用
     * 注：需要在有状态栏的fragment的最顶端加一个状态栏大小的view
     *
     * @param activity
     * @param alpha
     */
    fun setTranslucentForImageViewInFragment(
        activity: Activity,
        alpha: Int
    ) {
        setTranslucentStatusBar(activity, null, alpha)
    }

    /**
     * 简易的方法，不过这个状态栏是根据CollapsingToolbarLayout的颜色变的
     * 也就是说在5.0以上的系统当CollapsingToolbarLayout缩到最小的时候
     * 状态栏也是CollapsingToolbarLayout的颜色，不会是colorPrimaryDark的颜色
     * @param activity
     * @param toolbar
     * @param collapsingToolbarLayout
     */
    fun setCollapsingToolbar(
        activity: Activity,
        toolbar: Toolbar,
        collapsingToolbarLayout: CollapsingToolbarLayout?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            //设置toolbar的margin
            val layoutParams = toolbar
                .layoutParams as FrameLayout.LayoutParams
            layoutParams.topMargin = getStatusBarHeight(activity)
        }
        setStatusAlpha(activity, 0)
    }

    /**
     * CollapsingToolbarLayout状态栏(可折叠图片)
     * 5.0缩放到最小之后顶部状态栏的颜色是根据colorPrimaryDark的颜色走的
     *
     * @param activity
     * @param coordinatorLayout
     * @param appBarLayout
     * @param imageView
     * @param toolbar
     */
    fun setCollapsingToolbar(
        activity: Activity,
        coordinatorLayout: CoordinatorLayout,
        appBarLayout: AppBarLayout,
        imageView: ImageView,
        toolbar: Toolbar
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window
                .clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.argb(0, 0, 0, 0)
            appBarLayout.fitsSystemWindows = true
            imageView.fitsSystemWindows = true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            coordinatorLayout.fitsSystemWindows = false
            appBarLayout.fitsSystemWindows = false
            imageView.fitsSystemWindows = false
            toolbar.fitsSystemWindows = true
            val lp =
                toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
            lp.height = (getStatusBarHeight(activity) +
                    activity.resources
                        .getDimension(R.dimen.abc_action_bar_default_height_material)).toInt()
            toolbar.layoutParams = lp
            setTranslucentStatusViewToAct(activity, 0)
            //            setCollapsingToolbarStatus(activity, appBarLayout);
        }
    }

    /**
     * Android4.4上CollapsingToolbar折叠时statusBar显示和隐藏
     * 一般来说可以不用设置这个
     *
     * @param appBarLayout
     */
    private fun setCollapsingToolbarStatus(
        activity: Activity,
        appBarLayout: AppBarLayout
    ) {
        val contentView =
            activity.findViewById<View>(android.R.id.content) as ViewGroup
        val fakeTranslucentView =
            contentView.findViewById<View>(TRANSLUCENT_VIEW_ID)
        ViewCompat.setAlpha(fakeTranslucentView, 1f)
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val maxScroll = appBarLayout.totalScrollRange
            val percentage =
                Math.abs(verticalOffset).toFloat() / maxScroll.toFloat()
            ViewCompat.setAlpha(fakeTranslucentView, percentage)
        })
    }
    //----------------私有方法----------------------
    /**
     * 设置 DrawerLayout 属性
     *
     * @param drawerLayout              DrawerLayout
     * @param drawerLayoutContentLayout DrawerLayout 的内容布局
     */
    private fun setDrawerLayoutProperty(
        drawerLayout: DrawerLayout,
        drawerLayoutContentLayout: ViewGroup
    ) {
        val drawer = drawerLayout.getChildAt(1) as ViewGroup
        drawerLayout.fitsSystemWindows = false
        drawerLayoutContentLayout.fitsSystemWindows = false
        drawerLayoutContentLayout.clipToPadding = true
        drawer.fitsSystemWindows = false
    }

    /**
     * 设置状态栏view的颜色并添加到界面中，如果找到状态栏view则直接设置，否则创建一个再设置
     *
     * @param activity
     * @param color
     * @param statusBarAlpha
     */
    private fun setStatusViewToAct(
        activity: Activity,
        @ColorInt color: Int,
        statusBarAlpha: Int
    ) {
        val decorView =
            activity.window.decorView as ViewGroup
        val fakeStatusBarView =
            decorView.findViewById<View>(STATUS_VIEW_ID)
        if (fakeStatusBarView != null) {
            if (fakeStatusBarView.visibility == View.GONE) {
                fakeStatusBarView.visibility = View.VISIBLE
            }
            fakeStatusBarView.setBackgroundColor(
                statusColorIntensity(
                    color,
                    statusBarAlpha
                )
            )
        } else {
            decorView.addView(
                createStatusBarView(
                    activity,
                    color,
                    statusBarAlpha
                )
            )
        }
    }

    /**
     * 设置状态栏view的透明度，如果找到状态栏view则直接设置，否则创建一个再设置
     *
     * @param activity
     * @param statusBarAlpha
     */
    private fun setTranslucentStatusViewToAct(
        activity: Activity,
        statusBarAlpha: Int
    ) {
        setARGBStatusViewToAct(activity, 0, 0, 0, statusBarAlpha)
    }

    /**
     * 设置状态栏view的ARGB，如果找到状态栏view则直接设置，否则创建一个再设置
     *
     * @param activity
     * @param statusBarAlpha
     */
    private fun setARGBStatusViewToAct(
        activity: Activity,
        r: Int,
        g: Int,
        b: Int,
        statusBarAlpha: Int
    ) {
        val contentView =
            activity.findViewById<View>(android.R.id.content) as ViewGroup
        val fakeStatusBarView =
            contentView.findViewById<View>(TRANSLUCENT_VIEW_ID)
        if (fakeStatusBarView != null) {
            if (fakeStatusBarView.visibility == View.GONE) {
                fakeStatusBarView.visibility = View.VISIBLE
            }
            fakeStatusBarView.setBackgroundColor(
                Color.argb(
                    statusBarAlpha,
                    r,
                    g,
                    b
                )
            )
        } else {
            contentView.addView(
                createARGBStatusBarView(
                    activity,
                    r,
                    g,
                    b,
                    statusBarAlpha
                )
            )
        }
    }

    /**
     * 创建和状态栏一样高的矩形，用于改变状态栏颜色和明暗度
     *
     * @param activity
     * @param color
     * @param alpha
     * @return
     */
    private fun createStatusBarView(
        activity: Activity,
        @ColorInt color: Int,
        alpha: Int
    ): View {
        // 绘制一个和状态栏一样高的矩形
        val statusBarView = View(activity)
        val params =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(activity)
            )
        statusBarView.layoutParams = params
        statusBarView.setBackgroundColor(statusColorIntensity(color, alpha))
        statusBarView.id = STATUS_VIEW_ID
        return statusBarView
    }

    /**
     * 创建和状态栏一样高的矩形，用于改变状态栏透明度
     *
     * @param activity
     * @param alpha
     * @return
     */
    private fun createTranslucentStatusBarView(
        activity: Activity,
        alpha: Int
    ): View {
        return createARGBStatusBarView(activity, 0, 0, 0, alpha)
    }

    /**
     * 创建和状态栏一样高的矩形，用于改变状态栏ARGB
     *
     * @param activity
     * @param r
     * @param g
     * @param b
     * @param alpha
     * @return
     */
    private fun createARGBStatusBarView(
        activity: Activity,
        r: Int,
        g: Int,
        b: Int,
        alpha: Int
    ): View {
        // 绘制一个和状态栏一样高的矩形
        val statusBarView = View(activity)
        val params =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(activity)
            )
        statusBarView.layoutParams = params
        statusBarView.setBackgroundColor(Color.argb(alpha, r, g, b))
        statusBarView.id = TRANSLUCENT_VIEW_ID
        return statusBarView
    }

    /**
     * 得到statusbar高度
     *
     * @param activity
     * @return
     */
    private fun getStatusBarHeight(activity: Activity): Int {
        val resourceId =
            activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        return activity.resources.getDimensionPixelSize(resourceId)
    }

    /**
     * 计算状态栏颜色明暗度
     *
     * @param color color值
     * @param alpha alpha值
     * @return 最终的状态栏颜色
     */
    private fun statusColorIntensity(@ColorInt color: Int, alpha: Int): Int {
        if (alpha == 0) {
            return color
        }
        val a = 1 - alpha / 255f
        var red = color shr 16 and 0xff
        var green = color shr 8 and 0xff
        var blue = color and 0xff
        red = (red * a + 0.5).toInt()
        green = (green * a + 0.5).toInt()
        blue = (blue * a + 0.5).toInt()
        return 0xff shl 24 or (red shl 16) or (green shl 8) or blue
    }

    /**
     * 配置状态栏之下的View
     *
     * @param activity
     */
    fun setRootView(activity: Activity) {
        val parent =
            activity.findViewById<View>(android.R.id.content) as ViewGroup
        var i = 0
        val count = parent.childCount
        while (i < count) {
            val childView = parent.getChildAt(0)
            if (childView is ViewGroup) {
                childView.setFitsSystemWindows(true)
                childView.clipToPadding = true
            }
            i++
        }
    }
}