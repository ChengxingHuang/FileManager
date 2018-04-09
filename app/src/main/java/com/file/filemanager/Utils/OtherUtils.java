package com.file.filemanager.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.format.Formatter;

import com.file.filemanager.FileInfo;
import com.file.filemanager.R;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by huang on 2017/7/21.
 */

public class OtherUtils {
    private static final String UNIT_B = "B";
    private static final String UNIT_KB = "KB";
    private static final String UNIT_MB = "MB";
    private static final String UNIT_GB = "GB";
    private static final String UNIT_TB = "TB";

    private static final int UNIT_INTERVAL = 1024;
    private static final double ROUNDING_OFF = 0.005;
    private static final int DECIMAL_NUMBER = 100;

    private static final HashMap<String, Integer> sCustomDrawableIdsMap = new HashMap<String, Integer>();

    /**
     * 获取获取指定存储（手机/SD卡）的可用空间
     */
    public static String getStorageAvailableSpace(Context context, String path){
        StatFs stat = new StatFs(path);
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return context.getString(R.string.available)
                + Formatter.formatFileSize(context, blockSize * availableBlocks);
    }

    /**
     * 获取获取指定存储（手机/SD卡）的总空间
     */
    public static String getStorageTotalSpace(Context context, String path){
        StatFs stat = new StatFs(path);
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return context.getString(R.string.total)
                + Formatter.formatFileSize(context, blockSize * totalBlocks);
    }

    public static int getReflectInt(Object object, String id){
        int description = -1;

        if(null != object && null != id){
            try {
                Field field = object.getClass().getDeclaredField(id);
                field.setAccessible(true);
                description = field.getInt(object);

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e){
                e.printStackTrace();
            }
        }

        return description;
    }

    public static String getReflectString(Object object, String id){
        String description = null;

        if(null != object && null != id){
            try {
                Field field = object.getClass().getDeclaredField(id);
                field.setAccessible(true);
                description = (String)field.get(object);

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e){
                e.printStackTrace();
            }
        }

        return description;
    }

    public static boolean getReflectBoolean(Object object, String id){
        boolean description = false;

        if(null != object && null != id){
            try {
                Field field = object.getClass().getDeclaredField(id);
                field.setAccessible(true);
                description = field.getBoolean(object);

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e){
                e.printStackTrace();
            }
        }

        return description;
    }

    public static long getReflectLong(Object object, String id){
        long description = 0;

        if(null != object && null != id){
            try {
                Field field = object.getClass().getDeclaredField(id);
                field.setAccessible(true);
                description = field.getLong(object);

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e){
                e.printStackTrace();
            }
        }

        return description;
    }

    public static final File getReflectFile(Object object, String id){
        File description = null;

        if(null != object && null != id){
            try {
                Field field = object.getClass().getDeclaredField(id);
                field.setAccessible(true);
                description = (File)field.get(object);

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e){
                e.printStackTrace();
            }
        }

        return description;
    }

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
        double formatSize = ((double) sizeInt) / DECIMAL_NUMBER;

        if (formatSize == 0) {
            return "0" + " " + unit;
        } else {
            return Double.toString(formatSize) + " " + unit;
        }
    }

    // TODO: 2017/12/17 MediaStore好像不会自动更新 
    public static ArrayList<FileInfo> getSpecificTypeOfFile(Context context, String[] extensions) {
        ArrayList<FileInfo> list = new ArrayList<FileInfo>();
        Uri fileUri = MediaStore.Files.getContentUri("external");
        String[] projection = new String[]{MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.TITLE};
        String selection = "";
        for(int i = 0; i < extensions.length; i++){
            if(0 != i) {
                selection = selection + " OR ";
            }
            selection = selection + MediaStore.Files.FileColumns.DATA + " LIKE '%" + extensions[i] + "'";
        }

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(fileUri, projection, selection, null, null);
        if(cursor == null)
            return null;
        while(cursor.moveToNext()) {
            String path = cursor.getString(0);
            FileInfo fileInfo = new FileInfo(context, path);
            list.add(fileInfo);
        }
        cursor.close();

        return list;
    }

    // TODO: 2017/10/12   文件的MIME类型对应的图标，如果需要添加，添加在这里即可
    public static void mimeTypeInit(){
        // Audio:flac/mp3/mid/ogg/mp4a/wav/wma
        registerMimeType(R.drawable.file_type_audio,
                "application/x-flac",
                "audio/mpeg",
                "audio/midi",
                "audio/ogg",
                "audio/mp4",
                "audio/wav",
                "audio/x-ms-wma");

        // Image:bmp/jpg/jpeg/gif/png/tif/tiff/tga/psd
        registerMimeType(R.drawable.file_type_image,
                "image/x-ms-bmp",
                "image/jpeg",
                "image/gif",
                "image/png",
                "image/tiff",
                "image/x-targa",
                "image/vnd.adobe.photoshop");

        // Video:avi/dv/mp4/mpeg/mpg/mov/wm/flv/mkv
        registerMimeType(R.drawable.file_type_video,
                "video/x-msvideo",
                "video/x-dv",
                "video/mp4",
                "video/mpeg",
                "video/quicktime",
                "video/x-ms-wmv",
                "video/x-flv",
                "video/x-matroska");

        // Text:txt
        registerMimeType(R.drawable.file_type_txt, "text/plain");

        // Source code
        registerMimeType(R.drawable.file_type_codes,
                "application/rdf+xml",
                "application/rss+xml",
                "application/x-object",
                "application/xhtml+xml",
                "text/css",
                "text/html",
                "text/xml",
                "text/x-c++hdr",
                "text/x-c++src",
                "text/x-chdr",
                "text/x-csrc",
                "text/x-dsrc",
                "text/x-csh",
                "text/x-haskell",
                "text/x-java",
                "text/x-literate-haskell",
                "text/x-pascal",
                "text/x-tcl",
                "text/x-tex",
                "application/x-latex",
                "application/x-texinfo",
                "application/atom+xml",
                "application/ecmascript",
                "application/json",
                "application/javascript",
                "application/xml",
                "text/javascript",
                "application/x-javascript");

        // Compressed:gz/tgz/bz/bz2/tbz/zip/rar/tar/7z
        registerMimeType(R.drawable.file_type_zip,
                "application/x-gzip",
                "application/x-bzip2",
                "application/zip",
                "application/x-rar",
                "application/x-tar",
                "application/x-7z-compressed");

        // Contact
        registerMimeType(R.drawable.file_type_vcf,
                "text/x-vcard",
                "text/vcard");

        // PDF
        registerMimeType(R.drawable.file_type_pdf, "application/pdf");

        // PPT
        registerMimeType(R.drawable.file_type_ppt,
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.openxmlformats-officedocument.presentationml.template",
                "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
                "application/vnd.stardivision.impress",
                "application/vnd.sun.xml.impress",
                "application/vnd.sun.xml.impress.template",
                "application/x-kpresenter",
                "application/vnd.oasis.opendocument.presentation");

        // Excel
        registerMimeType(R.drawable.file_type_ppt,
                "application/vnd.oasis.opendocument.spreadsheet",
                "application/vnd.oasis.opendocument.spreadsheet-template",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
                "application/vnd.stardivision.calc",
                "application/vnd.sun.xml.calc",
                "application/vnd.sun.xml.calc.template",
                "application/x-kspread",
                "text/comma-separated-values");

        // Word
        registerMimeType(R.drawable.file_type_word,
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
                "application/vnd.oasis.opendocument.text",
                "application/vnd.oasis.opendocument.text-master",
                "application/vnd.oasis.opendocument.text-template",
                "application/vnd.oasis.opendocument.text-web",
                "application/vnd.stardivision.writer",
                "application/vnd.stardivision.writer-global",
                "application/vnd.sun.xml.writer",
                "application/vnd.sun.xml.writer.global",
                "application/vnd.sun.xml.writer.template",
                "application/x-abiword",
                "application/x-kword",
                "text/markdown");
    }

    public static HashMap<String, Integer> getRegisterMimeTypeMap(){
        return sCustomDrawableIdsMap;
    }

    private static void registerMimeType(int resId, String... mimeTypes) {
        for (String type : mimeTypes) {
            sCustomDrawableIdsMap.put(type, resId);
        }
    }
}
