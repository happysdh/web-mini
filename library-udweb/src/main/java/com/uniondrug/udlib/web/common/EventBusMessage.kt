package com.uniondrug.udlib.web.common

class EventBusMessage<T> {
    var dataList: List<T>? = null
    var msgType: Int
    var data: T? = null
    var dataMore: T? = null
    var extraData: String? = null

    constructor(msgType: Int) {
        this.msgType = msgType
    }

    constructor(msgType: Int, list: List<T>?) {
        this.msgType = msgType
        dataList = list
    }

    constructor(msgType: Int, data: T) {
        this.msgType = msgType
        this.data = data
    }

    constructor(msgType: Int, data: T, dataMore: T) {
        this.msgType = msgType
        this.data = data
        this.dataMore = dataMore
    }

    constructor(msgType: Int, data: T, extraData: String?) {
        this.msgType = msgType
        this.data = data
        this.extraData = extraData
    }

    companion object {
        const val MSG_TYPE_APP_TO_MP = 100001 //APP向小程序发消息
        const val MSG_TYPE_APP_FINISH = 100002 //关闭
        const val MSG_TYPE_APP_REFRESH = 100003 //刷新
        const val MSG_TYPE_APP_RELOAD_DATA = 100004 //刷新数据
        const val MSG_TYPE_H5_QRCODE = 44 //H5 扫二维码
        const val MSG_TYPE_PRIVILEGE_GET = 45 //H5 特权领取
        const val MSG_TYPE_WEBVIEW_PRESCRIPTION_NEW = 46 //处方药
    }
}