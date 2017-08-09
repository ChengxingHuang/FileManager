package com.file.filemanager;

import android.content.Context;

/**
 * Created by huang on 2017/7/21.
 */

public class Utils {
    /**
     * 获取获取指定存储（手机/SD卡）的可用空间
     */
    public static String getStorageAvailableSpace(Context context, String path){
        return "Available:6GB";
    }

    /**
     * 获取获取指定存储（手机/SD卡）的总空间
     */
    public static String getStorageTotalSpace(Context context, String path){
        return "Total:6GB";
    }
}
