package com.uniondrug.udlib.web.common

object JSConst {
    //SDK提供功能
    const val JSCONST_UDUPIMAGE = "UDupImage" //上传图片
    const val JSCONST_UDTEL = "UDtoTel" //拨打电话
    const val JSCONST_UDSCAN = "UDsetScavenging" //扫一扫
    const val JSCONST_UDDOWNIMAGE = "UDdownImage" //保存图片到图库
    const val JSCONST_UDTONAVIGATE = "UDtoNavigate" //去导航
    const val JSCONST_UDCOPY = "UDcopy" //复制
    const val JSCONST_UDFINISH = "UDfinish" //销毁当前页
    const val JSCONST_UDNAVIGATETO = "UDnavigateTo" //新页面打开
    const val JSCONST_UDRELOADDATA = "UDreloadData" //通知H5刷新页面

    const val JSCONST_UDSETHEADBARHIDDEN = "UDsetHeadBarHidden" //是否隐藏头部导航
    const val JSCONST_UDAPPTOJS = "UDAppToJs" //APP 发给 H5消息
    const val JSCONST_UDJSTOAPP = "UDJsToApp" //H5  发给 APP消息

    const val JSCONST_UDWXSHARE = "UDwxShare" //分享
    const val JSCONST_UDSERVICESHARE = "UDserviceShare" //设置分享内容
    const val JSCONST_UDSETSHAREHIDDEN = "UDsetShareHidden" //是否隐藏分享按钮
    const val JSCONST_UD_UPLOAD_FILE = "UDUpLoadFile" //上传文件
    const val JSCONST_UD_DOWNLOAD_FILE = "UDDownLoadFile" //下载文件
    const val JSCONST_UD_SHOW_SHARE_WX = "UDShowShareWX" //显示画报分享按钮
    const val JSCONST_UD_HIDE_SHARE_WX = "UDHiddenShareWX" //显示画报分享按钮
    const val JSCONST_UD_IMG_SHARE_WX = "UDImgShareWX" //通知二维码修改

    //需要信息中转
    const val JSCONST_UDUSERINFO = "UDuserInfo" //用户信息
    const val JSCONST_UDVERSION = "UDAppVersion" //版本
    const val JSCONST_UDBACKTOHOME = "UDbackToHome" //回到首页
    const val JSCONST_UDTOFEEDBACK = "UDtoFeedBack" //截屏并去意见反馈
    const val JSCONST_UDDTPLIST = "UDDtpList" //进入DTP列表
    const val JSCONST_UDPOINTSFEEDBACK = "UDpointsFeedback" //意见反馈
    const val JSCONST_BACK_TO_MEMBER_ROOT = "UDbackToMemberRoot" //回到会员首页
    const val JSCONST_TO_MEMBER = "UDtoMember" //商保新会员，进入用户详情
    const val JSCONST_UD_OPEN_LIVE = "UDOpenLive" //开播
    const val JSCONST_UD_WATCH_LIVE = "UDWatchLive" //看播
    const val JSCONST_UD_AUTH = "UDToRealNameAuthentication" //跳转实名
    const val JSCONST_UD_MEDICINE_AUTH = "UDToPrivilegeAttestation" //跳转实名
    const val JSCONST_UD_PAY = "UDUseAccountVC" //支付页面
    const val JSCONST_UD_WATCH_AGORA = "UDWatchLiveAgora" //声网
    const val JSCONST_UDMPSHARE = "UDMPShare" //
    const val JSCONST_UDSHOWTAB = "UDShowTab" //显示隐藏TAB
    const val JSCONST_UDTOORIGINVIEW = "UDtoOriginalView" //跳原生页面
    const val JSCONST_SUBMIT= "submitFromWeb" //跳原生页面


    //应该废弃需H5自己完成
    const val JSCONST_UDPOSTRECIPEDETAIL = "UDpostRecipeDetail" //获取当前所有处方单
    const val JSCONST_UDBACKRECIPEDETAIL = "UDbackRecipeDetail" //更新最新处方单
    const val JSCONST_UDPRESCRIPTIONDRUG = "UDprescriptionDrug" //处方药返回
}