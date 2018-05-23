package com.file.filemanager.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by huang on 2018/5/22.
 */

public class PermissionUtils {

    public static final String PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 0x01;
    public static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0x02;

    public static void requestPermission(Activity context, String permission, int requestCode){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.requestPermissions(new String[]{permission}, requestCode);
        }
    }

    public static boolean hasPermission(Activity context, String permission){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        }

        return true;
    }

    public static boolean showRequestPermissionRationale(Activity context, String permission){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.shouldShowRequestPermissionRationale(permission);
        }

        return true;
    }
}
