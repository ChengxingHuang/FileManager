package com.file.filemanager;

import android.content.Context;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by huang on 2017/7/21.
 */

public class FileUtils {
    private static final String TAG = "FileUtils";

    public static final String UNIT_B = "B";
    public static final String UNIT_KB = "KB";
    public static final String UNIT_MB = "MB";
    public static final String UNIT_GB = "GB";
    public static final String UNIT_TB = "TB";

    private static final int UNIT_INTERVAL = 1024;
    private static final double ROUNDING_OFF = 0.005;
    private static final int DECIMAL_NUMBER = 100;

    /**
     * 将byte转换为B/KB/MB/GB/TB
     */
    public static String sizeToHumanString(long size) {
        String unit = UNIT_B;
        if (size < DECIMAL_NUMBER) {
            return Long.toString(size) + " " + unit;
        }

        unit = UNIT_KB;
        double sizeDouble = (double) size / (double) UNIT_INTERVAL;
        if (sizeDouble > UNIT_INTERVAL) {
            sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
            unit = UNIT_MB;
        }
        if (sizeDouble > UNIT_INTERVAL) {
            sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
            unit = UNIT_GB;
        }
        if (sizeDouble > UNIT_INTERVAL) {
            sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
            unit = UNIT_TB;
        }

        long sizeInt = (long) ((sizeDouble + ROUNDING_OFF) * DECIMAL_NUMBER);
        double formatedSize = ((double) sizeInt) / DECIMAL_NUMBER;

        if (formatedSize == 0) {
            return "0" + " " + unit;
        } else {
            return Double.toString(formatedSize) + " " + unit;
        }
    }

    /**
     * 获取文件夹中工包含多少子文件
     */
    public static String getChildFolderFileCount(Context context, String parentPath){
        int count = new File(parentPath).list().length;
        if(0 == count){
            return context.getResources().getString(R.string.empty);
        }else{
            return count + "";
        }
    }

    /**
     * 获取文件/文件夹的最后修改日期
     */
    public static String getLastChangeDate(File file){
        long lastChangeTime = file.lastModified();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(lastChangeTime);
    }

    /**
     * 获取文件/文件夹的最后修改时间
     */
    public static String getLastChangeTime(File file){
        long lastChangeTime = file.lastModified();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(lastChangeTime);
    }

    /**
     * 获取获取指定存储（手机/SD卡）的可用空间
     */
    public static String getStorageAvailableSpace(String path){
        return "Available:6GB";
    }

    /**
     * 获取获取指定存储（手机/SD卡）的总空间
     */
    public static String getStorageTotalSpace(String path){
        return "Total:6GB";
    }
}
