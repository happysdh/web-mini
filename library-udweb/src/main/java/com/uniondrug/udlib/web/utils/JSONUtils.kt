package com.uniondrug.udlib.web.utils

import org.json.JSONObject

class JSONUtils {
    companion object {
        @JvmStatic
        fun stringToObj(str: String): JSONObject? {
            val jsonObj: JSONObject?
            try {
                jsonObj = JSONObject(str)
            } catch (e: Exception) {
                return null
            }
            return jsonObj
        }

        @JvmStatic
        fun getStrFromObj(obj: JSONObject, key: String): String {
            val str: String
            try {
                str = obj.getString(key)
            } catch (e: Exception) {
                return ""
            }
            return str
        }
    }
}