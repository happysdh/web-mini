package com.uniondrug.udlib.web.bean

class ToJSBean(success: Boolean, var data: Any) {
    var status: String
    var msg: String

    init {
        status = if (success) "1" else "0"
        if (success) {
            msg = "成功"
        } else {
            if (data is String) {
                msg = data as String
            } else {
                msg = "失败"
            }
        }
        msg = if (success) "成功" else (data as String)
    }
}