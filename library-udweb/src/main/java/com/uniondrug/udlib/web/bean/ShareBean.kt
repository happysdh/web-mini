package com.uniondrug.udlib.web.bean

data class ShareBean
    (
    var shareLink: String,
    var shareTitle: String,
    var shareDescr: String,
    var shareImg: String,
    var shareBigImg: String
) {
    var wxSession: String = ""
}