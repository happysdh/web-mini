package com.uniondrug.udlib.web.utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileSaveUtils {
//    public static String getFilePathFromURI(Context context, Uri contentUri) {
//        //copy file and send new file path
//        String fileName = getFileName(contentUri);
//        if (!TextUtils.isEmpty(fileName)) {
//            File copyFile = new File(TEMP_DIR_PATH + File.separator + fileName);
//            copy(context, contentUri, copyFile);
//            return copyFile.getAbsolutePath();
//        }
//        return null;
//    }
//
//    public static String getFileName(Uri uri) {
//        if (uri == null) return null;
//        String fileName = null;
//        String path = uri.getPath();
//        int cut = path.lastIndexOf('/');
//        if (cut != -1) {
//            fileName = path.substring(cut + 1);
//        }
//        return fileName;
//    }

//    public static void copy(Context context, Uri srcUri, File dstFile) {
//        try {
//            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
//            if (inputStream == null) return;
//            OutputStream outputStream = new FileOutputStream(dstFile);
//            IOUtils.copyStream(inputStream, outputStream);
//            inputStream.close();
//            outputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        final File file = new File(context.getCacheDir(), uri.getLastPathSegment());
//        try (final InputStream inputStream = context.getContentResolver().openInputStream(uri);
//             OutputStream output = new FileOutputStream(file)) {
//            // You may need to change buffer size. I use large buffer size to help loading large file , but be ware of
//            //  OutOfMemory Exception
//            final byte[] buffer = new byte[8 * 1024];
//            int read;
//
//            while ((read = inputStream.read(buffer)) != -1) {
//                output.write(buffer, 0, read);
//            }
//            output.flush();
//            return file.getPath();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        return "";
//    }
}
