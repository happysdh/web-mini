package com.uniondrug.udlib.web.activity

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.uniondrug.udlib.web.UDWebSDK
import com.uniondrug.udlib.web.http.HttpUtils
import com.uniondrug.udlib.web.http.VersionBean
import com.uniondrug.udlib.web.utils.JSONUtils
import com.uniondrug.udlib.web.utils.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class UDWebViewModel(application: Application) : AndroidViewModel(application) {
    var resLiveData = MutableLiveData<VersionBean>()
    var netUrlData = MutableLiveData<String>()
    var fileUrlData = MutableLiveData<String>()
    var errorEvent = MutableLiveData<Boolean>()
    fun getVersions(projectName: String, assistantId: String?) {
        /*viewModelScope是一个绑定到当前viewModel的作用域  当ViewModel被清除时会自动取消该作用域，所以不用担心内存泄漏为问题*/
        viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    val requestURL = "${getHost()}/release/detail"
                    var writer: BufferedWriter? = null
                    try {
                        val url = URL(requestURL)
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "POST"
                        //不使用缓存
                        conn.useCaches = false
                        //设置超时时间
                        conn.connectTimeout = 30000
                        //设置读取超时时间
                        conn.readTimeout = 30000
                        //设置为 true 后才能写入参数
                        conn.doOutput = true
                        conn.setRequestProperty("Content-Type", "application/json")

                        conn.connect()
                        writer = BufferedWriter(OutputStreamWriter(conn.outputStream, "UTF-8"))
                        if (assistantId != null && assistantId.isNotEmpty()) {
                            writer.write("{\"projectName\":\"$projectName\",\"assistantId\":\"$assistantId\"}")
                        } else {
                            writer.write("{\"projectName\":\"$projectName\"}")
                        }
                        writer.flush()

                        val json = JSONObject(HttpUtils.getRetString(conn.inputStream))
                        Log.w("UDWEB", "getVersions: $requestURL < - > $projectName->$json")
                        val data = json.getJSONObject("data")
                        val bean = VersionBean()
                        bean.router = data.getString("router")
                        bean.version = data.getString("version")
                        bean.downUrl = data.getString("downUrl")
                        bean.project = data.getString("project")
                        bean.status = data.getString("status")
                        bean.unionToken = data.getString("unionToken")
                        return@withContext bean
                    } catch (e: Exception) {
                        val bean = VersionBean()
                        bean.status = "0"
                        ToastUtils.showShortToast(getApplication(), e.toString())
                        return@withContext bean
                    } finally {
                        if (writer != null) {
                            try {
                                writer.close()
                                writer = null
                            } catch (e: IOException) {
                            }
                        }
                    }
                }
                if ("0" == data.status) {
                    errorEvent.value = true
                } else {
                    resLiveData.value = data
                }
            } catch (e: Exception) {
                ToastUtils.showShortToast(getApplication(), e.toString())
                errorEvent.value = true
            }
        }
    }

    fun uploadImage(filePath: String) {
        viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    var netImageUrl = ""
                    val connection: HttpURLConnection?
                    val dos: DataOutputStream?
                    val fin: FileInputStream?
                    val boundary = "---------------------------265001916915724"
                    val urlServer = "${getHost()}/file/upload"
                    val lineEnd = "\r\n"
                    var bytesAvailable: Int
                    var bufferSize: Int
                    var bytesRead: Int
                    val maxBufferSize = 1 * 1024 * 512
                    val buffer: ByteArray?
                    val url = URL(urlServer)
                    connection = url.openConnection() as HttpURLConnection

                    // 允许向url流中读写数据
                    connection.doInput = true
                    connection.doOutput = true
                    connection.useCaches = true

                    // 启动post方法
                    connection.requestMethod = "POST"

                    // 设置请求头内容
                    connection.setRequestProperty("connection", "Keep-Alive")
                    connection.setRequestProperty("Content-Type", "text/plain")

                    // 伪造请求头
                    connection.setRequestProperty(
                        "Content-Type",
                        "multipart/form-data; boundary=$boundary"
                    )

                    // 开始设置POST Data里面的数据
                    dos = DataOutputStream(connection.outputStream)
                    fin = FileInputStream(filePath)
                    //--------------------开始伪造上传images.jpg的信息-----------------------------------
                    val fileMeta = "--" + boundary + lineEnd +
                            "Content-Disposition: form-data; name=\"file\"; filename=\"" + filePath + "\"" + lineEnd +
                            "Content-Type: image/jpeg" + lineEnd + lineEnd
                    // 向流中写入fileMeta
                    dos.write(fileMeta.toByteArray())

                    // 取得本地图片的字节流，向url流中写入图片字节流
                    bytesAvailable = fin.available()
                    bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
                    buffer = ByteArray(bufferSize)
                    bytesRead = fin.read(buffer, 0, bufferSize)
                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize)
                        bytesAvailable = fin.available()
                        bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
                        bytesRead = fin.read(buffer, 0, bufferSize)
                    }
                    dos.writeBytes(lineEnd + lineEnd)
                    // POST Data结束
                    dos.writeBytes("--$boundary--")
                    fin.close();
                    dos.flush()
                    dos.close()

                    // Getting byte input stream from connection object
                    val inputStream: InputStream = connection.inputStream
                    // Instantiate character input stream objects, wrapping byte streams into character streams
                    val inputStreamReader = InputStreamReader(inputStream)
                    // Create an input buffer object that passes in the input character stream object
                    val bufferedReader = BufferedReader(inputStreamReader)

                    // Create a string object to receive each string read from the input buffer
                    var line: String?
                    // Create a variable string object to load the final data of the buffer object, and store all the data in the response in the object using string addition
                    val stringBuilder = java.lang.StringBuilder()
                    // Read the buffer data line by line using a loop, and assign one line of string data to the line string variable each time until the read behavior space-time identifies the end of the loop.
                    while (bufferedReader.readLine().also { line = it } != null) {
                        // Append the data read by the buffer to the variable character object
                        stringBuilder.append(line)
                    }
                    // Close the open input stream in turn
                    bufferedReader.close()
                    inputStreamReader.close()
                    inputStream.close()

                    val obj = JSONUtils.stringToObj(stringBuilder.toString())
                    if (obj != null && "0" == JSONUtils.getStrFromObj(obj, "errno")) {
                        val data = JSONUtils.stringToObj(JSONUtils.getStrFromObj(obj, "data"))
                        if (data != null) {
                            netImageUrl = JSONUtils.getStrFromObj(data, "url")
                        }
                    }
                    return@withContext netImageUrl
                }
                netUrlData.value = data
            } catch (e: Exception) {
                netUrlData.value = ""
                ToastUtils.showShortToast(getApplication(), e.toString())
            }
        }
    }

    fun uploadPDF(filePath: String) {
        viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    var netImageUrl = ""
                    val connection: HttpURLConnection?
                    val dos: DataOutputStream?
                    val fin: FileInputStream?
                    val boundary = "---------------------------265001916915724"
                    val urlServer = "${getHost()}/file/upload"
                    val lineEnd = "\r\n"
                    var bytesAvailable: Int
                    var bufferSize: Int
                    var bytesRead: Int
                    val maxBufferSize = 1 * 1024 * 512
                    val buffer: ByteArray?
                    val url = URL(urlServer)
                    connection = url.openConnection() as HttpURLConnection

                    // 允许向url流中读写数据
                    connection.doInput = true
                    connection.doOutput = true
                    connection.useCaches = true

                    // 启动post方法
                    connection.requestMethod = "POST"

                    // 设置请求头内容
                    connection.setRequestProperty("connection", "Keep-Alive")
                    connection.setRequestProperty("Content-Type", "text/plain")

                    // 伪造请求头
                    connection.setRequestProperty(
                        "Content-Type",
                        "multipart/form-data; boundary=$boundary"
                    )

                    // 开始设置POST Data里面的数据
                    dos = DataOutputStream(connection.outputStream)
                    fin = FileInputStream(filePath)
                    //--------------------开始伪造上传images.jpg的信息-----------------------------------
                    val fileMeta = "--" + boundary + lineEnd +
                            "Content-Disposition: form-data; name=\"file\"; filename=\"" + filePath + "\"" + lineEnd +
                            "Content-Type: " + lineEnd + lineEnd
                    // 向流中写入fileMeta
                    dos.write(fileMeta.toByteArray())

                    // 取得本地图片的字节流，向url流中写入图片字节流
                    bytesAvailable = fin.available()
                    bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
                    buffer = ByteArray(bufferSize)
                    bytesRead = fin.read(buffer, 0, bufferSize)
                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize)
                        bytesAvailable = fin.available()
                        bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
                        bytesRead = fin.read(buffer, 0, bufferSize)
                    }
                    dos.writeBytes(lineEnd + lineEnd)
                    // POST Data结束
                    dos.writeBytes("--$boundary--")
                    fin.close();
                    dos.flush()
                    dos.close()

                    // Getting byte input stream from connection object
                    val inputStream: InputStream = connection.inputStream
                    // Instantiate character input stream objects, wrapping byte streams into character streams
                    val inputStreamReader = InputStreamReader(inputStream)
                    // Create an input buffer object that passes in the input character stream object
                    val bufferedReader = BufferedReader(inputStreamReader)

                    // Create a string object to receive each string read from the input buffer
                    var line: String?
                    // Create a variable string object to load the final data of the buffer object, and store all the data in the response in the object using string addition
                    val stringBuilder = java.lang.StringBuilder()
                    // Read the buffer data line by line using a loop, and assign one line of string data to the line string variable each time until the read behavior space-time identifies the end of the loop.
                    while (bufferedReader.readLine().also { line = it } != null) {
                        // Append the data read by the buffer to the variable character object
                        stringBuilder.append(line)
                    }
                    // Close the open input stream in turn
                    bufferedReader.close()
                    inputStreamReader.close()
                    inputStream.close()

                    val obj = JSONUtils.stringToObj(stringBuilder.toString())
                    if (obj != null && "0" == JSONUtils.getStrFromObj(obj, "errno")) {
                        val data = JSONUtils.stringToObj(JSONUtils.getStrFromObj(obj, "data"))
                        if (data != null) {
                            netImageUrl = JSONUtils.getStrFromObj(data, "url")
                        }
                    } else {

                    }
                    return@withContext netImageUrl
                }
                fileUrlData.value = data
            } catch (e: Exception) {
                fileUrlData.value = ""
                ToastUtils.showShortToast(getApplication(), e.toString())
            }
        }
    }

    private fun getHost(): String {
        return when (UDWebSDK.getInstance().environment) {
            1 -> {
                "https://pm-dstore-platform.turboradio.cn"
            }
            2 -> {
                "https://pm-dstore-platform.uniondrug.net"
            }
            3 -> {
                "https://pm-dstore-platform.uniondrug.cn"
            }
            else -> {
                "https://pm-dstore-platform.uniondrug.cn"
            }
        }
    }
}