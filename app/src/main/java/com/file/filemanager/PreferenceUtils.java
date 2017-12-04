package com.file.filemanager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by huang on 2017/9/25.
 */

public class PreferenceUtils {
    public static final String SHARE_PREFERENCE_NAME = "common_setting";
    public static final String COMMON_SETTING_ORDER_TYPE = "order_type";
    public static final String COMMON_SETTING_SHOW_HIDE_FILE = "show_hide_file";
    public static final String COMMON_SETTING_SHOW_SMALL_PICTURE = "show_small_picture";
    public static final String COMMON_SETTING_DIRECTOR_SORT_MODE = "director_sort_mode";
    public static final String COMMON_SETTING_SORT_BY = "sort_by";

    public static final boolean ORDER_TYPE_ASCEND = true;
    public static final boolean ORDER_TYPE_DESCEND = false;
    public static final int SORT_BY_NAME = 0;
    public static final int SORT_BY_SIZE = 1;
    public static final int SORT_BY_TYPE = 2;
    public static final int SORT_BY_LAST_MODIFY = 3;
    public static final int DIRECTOR_SORT_MODE_FOLDER_ON_TOP = 0;
    public static final int DIRECTOR_SORT_MODE_FILE_ON_TOP = 1;
    public static final int DIRECTOR_SORT_MODE_NONE_ON_TOP = 2;

    public static boolean getOrderTypeValue(Context context){
        SharedPreferences sh = context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sh.getBoolean(COMMON_SETTING_ORDER_TYPE, true);
    }

    public static void setOrderTypeValue(Context context, boolean sortType){
        SharedPreferences sh = context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sh.edit();
        editor.putBoolean(COMMON_SETTING_ORDER_TYPE, sortType);
        editor.commit();
    }

    public static boolean getShowHideFileValue(Context context){
        SharedPreferences sh = context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sh.getBoolean(COMMON_SETTING_SHOW_HIDE_FILE, false);
    }

    public static void setShowHideFileValue(Context context, boolean showHideFile){
        SharedPreferences sh = context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sh.edit();
        editor.putBoolean(COMMON_SETTING_SHOW_HIDE_FILE, showHideFile);
        editor.commit();
    }

    public static boolean getShowSmallPictureValue(Context context){
        SharedPreferences sh = context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sh.getBoolean(COMMON_SETTING_SHOW_SMALL_PICTURE, false);
    }

    public static void setShowSmallPictureValue(Context context, boolean showSmallPicture){
        SharedPreferences sh = context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sh.edit();
        editor.putBoolean(COMMON_SETTING_SHOW_SMALL_PICTURE, showSmallPicture);
        editor.commit();
    }

    public static int getDirectorSortModeValue(Context context){
        SharedPreferences sh = context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sh.getInt(COMMON_SETTING_DIRECTOR_SORT_MODE, 0);
    }

    public static void setDirectorSortModeValue(Context context, int mode){
        SharedPreferences sh = context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sh.edit();
        editor.putInt(COMMON_SETTING_DIRECTOR_SORT_MODE, mode);
        editor.commit();
    }

    public static int getSortByValue(Context context){
        SharedPreferences sh = context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sh.getInt(COMMON_SETTING_SORT_BY, 0);
    }

    public static void setSortByValue(Context context, int sort){
        SharedPreferences sh = context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sh.edit();
        editor.putInt(COMMON_SETTING_SORT_BY, sort);
        editor.commit();
    }
}
