package com.file.filemanager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by huang on 2017/9/25.
 */

public class PreferenceUtils {
    public static final String SHARE_PREFERENCE_NAME = "common_setting";
    public static final String COMMON_SETTING_SHOW_HIDE_FILE = "show_hide_file";
    public static final String COMMON_SETTING_FIRST_SHOW_FOLDER = "first_show_folder";
    public static final String COMMON_SETTING_IS_ASCENDING = "is_ascending";

    public static boolean canShowHideFile(Context context){
        SharedPreferences sh = context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sh.getBoolean(COMMON_SETTING_SHOW_HIDE_FILE, false);
    }

    public static boolean isFirstShowFolder(Context context){
        SharedPreferences sh = context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sh.getBoolean(COMMON_SETTING_FIRST_SHOW_FOLDER, true);
    }

    public static boolean isAscending(Context context){
        SharedPreferences sh = context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sh.getBoolean(COMMON_SETTING_IS_ASCENDING, true);
    }
}
