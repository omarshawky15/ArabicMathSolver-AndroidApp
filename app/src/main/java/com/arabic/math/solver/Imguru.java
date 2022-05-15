package com.arabic.math.solver;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class Imguru {
    public static File storeImage(Context context, Bitmap bmp) {
        String fileName = "IMG_" + DateFormat.format("yyyyMMdd_HH_mm_ss", Calendar.getInstance().getTime());
        String folderName = "ArabicMathSolver";
        File imageFile;
        Uri imageUri;
        OutputStream fos;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM" + File.separator + folderName);
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(imageUri);
                imageFile = new File(getPathFromUri(context, imageUri));
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM).toString() + File.separator + folderName;
                imageFile = new File(imagesDir);
                if (!imageFile.exists()) {
                    imageFile.mkdir();
                }
                imageFile = new File(imagesDir, fileName + ".png");
                fos = new FileOutputStream(imageFile);
            }
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return imageFile;
    }

    public static String getPathFromUri(Context context, Uri file_uri) {
        String result;
        String[] projection = new String[]{
                MediaStore.Images.Media.DATA
        };
        Cursor cursor = context.getContentResolver().query(file_uri, projection, null, null, null);
        if (cursor == null) {
            result = file_uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
