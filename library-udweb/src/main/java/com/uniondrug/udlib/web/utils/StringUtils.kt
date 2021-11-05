package com.uniondrug.udlib.web.utils

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import java.util.*
import java.util.regex.Pattern

class StringUtils {
    fun notNull(value: String?): Boolean {
        return value != null && !value.isEmpty()
    }

    companion object {
        fun isDoubleString(text: String?): Boolean {
            if (text == null) return false
            try {
                java.lang.Double.valueOf(text)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            return true
        }

        fun getPercentString(percent: Float): String {
            return String.format(Locale.US, "%d%%", (percent * 100).toInt())
        }

        fun isZero(text: String?): Boolean {
            if (text == null || text.isEmpty()) return true
            return if (text == "0" || text == "0.0" || text == "0.00") true else false
        }

        /**
         * 删除字符串中的空白符
         *
         * @param content
         * @return String
         */
        fun removeBlanks(content: String?): String? {
            if (content == null) {
                return null
            }
            val buff = StringBuilder()
            buff.append(content)
            for (i in buff.length - 1 downTo 0) {
                if (' ' == buff[i] || '\n' == buff[i] || '\t' == buff[i]
                    || '\r' == buff[i]
                ) {
                    buff.deleteCharAt(i)
                }
            }
            return buff.toString()
        }

        /**
         * 获取32位uuid
         *
         * @return
         */
        fun get32UUID(): String {
            return UUID.randomUUID().toString().replace("-".toRegex(), "")
        }

        fun isEmpty(input: String?): Boolean {
            return TextUtils.isEmpty(input)
        }

        /**
         * 生成唯一号
         *
         * @return
         */
        fun get36UUID(): String {
            val uuid = UUID.randomUUID()
            return uuid.toString()
        }

        fun filterUCS4(str: String): String {
            if (TextUtils.isEmpty(str)) {
                return str
            }
            if (str.codePointCount(0, str.length) == str.length) {
                return str
            }
            val sb = StringBuilder()
            var index = 0
            while (index < str.length) {
                val codePoint = str.codePointAt(index)
                index += Character.charCount(codePoint)
                if (Character.isSupplementaryCodePoint(codePoint)) {
                    continue
                }
                sb.appendCodePoint(codePoint)
            }
            return sb.toString()
        }

        /**
         * counter ASCII character as one, otherwise two
         *
         * @param str
         * @return count
         */
        fun counterChars(str: String): Int {
            // return
            if (TextUtils.isEmpty(str)) {
                return 0
            }
            var count = 0
            for (i in 0 until str.length) {
                val tmp = str[i].toInt()
                count += if (tmp > 0 && tmp < 127) {
                    1
                } else {
                    2
                }
            }
            return count
        }

        fun toInt(str: String): Int {
            var ret = 0
            try {
                ret = Integer.valueOf(str)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ret
        }

        fun nNumberCharacters(character: String?, number: Int): String {
            if (character == null || character.isEmpty() || number <= 0) {
                return ""
            }
            var temp = ""
            for (i in 0 until number) {
                temp += character
            }
            return temp
        }

        fun isNumeric(str: String): Boolean {
            for (i in 0 until str.length) {
                if (!Character.isDigit(str[i])) {
                    return false
                }
            }
            return true
        }

        fun encodeMobile(mobile: String): String {
            return mobile.substring(0, 3) + "****" + mobile.substring(7, 11)
        }

        /**
         * 判断字符串是否是金额
         *
         * @param str
         * @return
         */
        fun isPriceNumber(str: String?): Boolean {
            if (str == null || str.isEmpty()) return false
            val pattern =
                Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$") // 判断小数点后2位的数字的正则表达式
            val match = pattern.matcher(str)
            return match.matches()
        }

        fun getRandomString(len: Int): String {
            var returnStr: String? = ""
            val ch = CharArray(len)
            val rd = Random()
            for (i in 0 until len) {
                ch[i] = (rd.nextInt(9) + 65).toChar()
                ch[i] = encodeTable[rd.nextInt(36)]
            }
            returnStr = String(ch)
            return returnStr
        }

        private val encodeTable = charArrayOf(
            'A',
            'B',
            'C',
            'D',
            'E',
            'F',
            'G',
            'H',
            'I',
            'J',
            'K',
            'L',
            'M',
            'N',
            'O',
            'P',
            'Q',
            'R',
            'S',
            'T',
            'U',
            'V',
            'W',
            'X',
            'Y',
            'Z',
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9'
        )

        fun setNumColor(str: String, color: Int): SpannableStringBuilder {
            val style =
                SpannableStringBuilder(str)
            for (i in 0 until str.length) {
                val a = str[i]
                if (a >= '0' && a <= '9') {
                    style.setSpan(
                        ForegroundColorSpan(color),
                        i,
                        i + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            return style
        }

        fun setTextColor(
            str: String,
            key: String,
            color: Int
        ): SpannableStringBuilder {
            val style =
                SpannableStringBuilder(str)
            var index = 0
            val len = key.length
            while (str.indexOf(key, index) != -1) {
                val start = str.indexOf(key, index)
                style.setSpan(
                    ForegroundColorSpan(color),
                    start,
                    start + len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                index = start + len
            }
            return style
        }
    }
}