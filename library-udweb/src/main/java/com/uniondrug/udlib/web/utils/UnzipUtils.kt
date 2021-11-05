package com.uniondrug.udlib.web.utils


import java.io.*
import java.lang.Exception
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object UnzipUtils {

    @JvmStatic
    @Throws(IOException::class)
    open fun unzip(zipFile: File?, targetDirectory: File?): Unit {
        val zis = ZipInputStream(BufferedInputStream(FileInputStream(zipFile)))
        try {
            var ze: ZipEntry
            var count: Int
            val buffer = ByteArray(8192)
            while (zis.nextEntry.also { ze = it } != null) {
                //mac 打包多的文件
                if(ze.name.startsWith("__MACOSX"))continue
                val file = File(targetDirectory, ze.name)
                val dir = if (ze.isDirectory) file else file.parentFile
                if (!dir.isDirectory && !dir.mkdirs()) throw FileNotFoundException("Failed to ensure directory: " +
                        dir.absolutePath)
                if (ze.isDirectory) continue
                val fout = FileOutputStream(file)
                try {
                    while (zis.read(buffer).also { count = it } != -1) fout.write(buffer, 0, count)
                } catch (e: Exception) {
                    fout.close()
                } finally {
                    fout.close()
                }
                /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } catch (e: Exception) {
            zis.close()
        } finally {
            zis.close()
        }
    }
}