package com.uniondrug.udlib.web.utils;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileMSFUtils {
    public static String getPath(Context context, Uri uri) {
        final File file = new File(context.getCacheDir(), uri.getLastPathSegment());
        try (final InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream output = new FileOutputStream(file)) {
            // You may need to change buffer size. I use large buffer size to help loading large file , but be ware of
            //  OutOfMemory Exception
            final byte[] buffer = new byte[8 * 1024];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            return file.getPath();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
