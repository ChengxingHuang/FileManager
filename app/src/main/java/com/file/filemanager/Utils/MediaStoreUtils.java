package com.file.filemanager.Utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by huang on 2018/5/16.
 */

public class MediaStoreUtils {

    public static void scanPathForMediaStore(Context context, String path) {
        if (context != null && !TextUtils.isEmpty(path)) {
            String[] paths = { path };
            MediaScannerConnection.scanFile(context, paths, null, null);
        }
    }

    public static void scanPathForMediaStore(Context context, List<String> scanPaths) {
        int length = scanPaths.size();
        if (context != null && length > 0) {
            String[] paths = new String[length];
            scanPaths.toArray(paths);
            MediaScannerConnection.scanFile(context, paths, null, null);
        }
    }

    public static void deleteFileInMediaStore(Context context, String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        Uri uri = MediaStore.Files.getContentUri("external");
        String where = MediaStore.Files.FileColumns.DATA + "=?";
        String[] whereArgs = new String[] { path };
        if (context != null) {
            ContentResolver cr = context.getContentResolver();
            cr.delete(uri, where, whereArgs);
        }
    }

    public static void updateInMediaStore(Context context, String oldPath, String newPath){
        Uri uri = MediaStore.Files.getContentUri("external");
        String where = MediaStore.Files.FileColumns.DATA + "=?";
        String[] whereArgs = new String[] { oldPath };
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();

        values.put(MediaStore.Files.FileColumns.DATA, newPath);
        cr.update(uri, values, where, whereArgs);

        scanPathForMediaStore(context, newPath);
    }
}
