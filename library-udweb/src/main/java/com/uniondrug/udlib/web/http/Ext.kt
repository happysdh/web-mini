package com.uniondrug.udlib.web.http

/*数据解析扩展函数*/
fun <T> BaseResp<T>.dataConvert(): T {
    if ("0" == errno) {
        return data
    } else {
        throw Exception(error)
    }
}