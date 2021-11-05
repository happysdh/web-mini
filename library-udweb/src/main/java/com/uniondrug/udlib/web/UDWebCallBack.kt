package com.uniondrug.udlib.web

import com.github.lzyzsd.jsbridge.CallBackFunction

interface UDWebCallBack {
    fun onCallBack(event: String, data: String)
}