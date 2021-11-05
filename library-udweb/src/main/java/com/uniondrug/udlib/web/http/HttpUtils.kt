package com.uniondrug.udlib.web.http

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class HttpUtils {
    companion object {
        @JvmStatic
        fun getRetString(`is`: InputStream?): String {
            val buf: String
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                }
                `is`!!.close()
                buf = sb.toString()
                return buf
            } catch (e: Exception) {
            } finally {
                if (reader != null) {
                    try {
                        reader.close()
                        reader = null
                    } catch (e: IOException) {
                    }
                }
                if (`is` != null) {
                    try {
                        `is`.close()
                    } catch (e: IOException) {
                    }
                }
            }
            return ""
        }
    }

}