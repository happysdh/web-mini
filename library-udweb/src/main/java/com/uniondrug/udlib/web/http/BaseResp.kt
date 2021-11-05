package com.uniondrug.udlib.web.http

data class BaseResp<T>(
    var errno: String = "",
    var error: String = "",
    var `data`: T
)