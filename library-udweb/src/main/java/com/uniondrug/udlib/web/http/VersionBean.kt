package com.uniondrug.udlib.web.http

data class VersionBean(
    var project: String = "",
    var version: String = "",
    var downUrl: String = "",
    var router: String = "",
    var status: String = "",
    var unionToken: String = ""
)