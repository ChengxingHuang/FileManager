package com.file.filemanager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

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
}
