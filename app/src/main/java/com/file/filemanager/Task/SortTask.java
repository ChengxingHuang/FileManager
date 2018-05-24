package com.file.filemanager.Task;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.file.filemanager.FavoriteDBHandler;
import com.file.filemanager.FileInfo;
import com.file.filemanager.MountStorageManager;
import com.file.filemanager.Service.FileOperatorListener;

import java.io.File;
import java.util.ArrayList;

import static com.file.filemanager.CategoryFragment.TYPE_APK;
import static com.file.filemanager.CategoryFragment.TYPE_DOCUMENT;
import static com.file.filemanager.CategoryFragment.TYPE_FAVORITE;
import static com.file.filemanager.CategoryFragment.TYPE_IMAGE;
import static com.file.filemanager.CategoryFragment.TYPE_MUSIC;
import static com.file.filemanager.CategoryFragment.TYPE_QQ;
import static com.file.filemanager.CategoryFragment.TYPE_VIDEO;
import static com.file.filemanager.CategoryFragment.TYPE_WECHAT;
import static com.file.filemanager.CategoryFragment.TYPE_ZIP;

/**
 * Created by huang on 2018/1/18.
 */

public class SortTask extends BaseAsyncTask {
    private static final String[] IMAGE_EXTENSIONS = {".jpg", ".png", ".jpeg", ".gif", ".bmp"};
    private static final String[] MUSIC_EXTENSIONS = {".mp3", ".wma", ".amr", ".aac", ".flac", ".ape", ".midi", ".ogg"};
    private static final String[] VIDEO_EXTENSIONS = {".mpeg", ".avi", ".mov", ".wmv", ".3gp", ".mkv", ".mp4", ".rmvb"};
    private static final String[] DOCUMENT_EXTENSIONS = {".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".pdf"};
    private static final String[] APK_EXTENSIONS = {".apk"};
    private static final String[] ARCHIVE_EXTENSIONS = {".zip", ".rar", ".tar", ".gz", ".7z"};

    private static final String PATH_WECHAT = "tencent/MicroMsg";
    private static final String PATH_QQ = "tencent/QQfile_recv";
    private static final String PATH_QQ2 = "tencent/TIMfile_recv";

    private static final int IMAGE_MIN_SIZE = 1024 * 5;
    private static final int MUSIC_MIN_SIZE = 1024 * 10;
    private static final int VIDEO_MIN_SIZE = 1024 * 100;
    private static final int DOCUMENT_MIN_SIZE = 1024;
    private static final int ARCHIVE_MIN_SIZE = 1024 * 10;

    private Context mContext;
    private TaskInfo mTaskInfo;

    public SortTask(Context context, FileOperatorListener listener){
        super(listener);
        mContext = context;
        mTaskInfo = new TaskInfo();
    }

    @Override
    protected TaskInfo doInBackground(Void... params) {
        // QQ and WeChat
        MountStorageManager storageManager = MountStorageManager.getInstance();
        ArrayList<MountStorageManager.MountStorage> mountStorageList = storageManager.getMountStorageList();
        for(int i = 0; i < mountStorageList.size(); i++){
            getTencentFiles(mountStorageList.get(i).mPath + "/" + PATH_QQ, TYPE_QQ);
            getTencentFiles(mountStorageList.get(i).mPath + "/" + PATH_QQ2, TYPE_QQ);
            getTencentFiles(mountStorageList.get(i).mPath + "/" + PATH_WECHAT, TYPE_WECHAT);
        }

        // Media type
        getMediaFiles(IMAGE_EXTENSIONS, TYPE_IMAGE);
        getMediaFiles(MUSIC_EXTENSIONS, TYPE_MUSIC);
        getMediaFiles(VIDEO_EXTENSIONS, TYPE_VIDEO);
        getMediaFiles(DOCUMENT_EXTENSIONS, TYPE_DOCUMENT);
        getMediaFiles(APK_EXTENSIONS, TYPE_APK);
        getMediaFiles(ARCHIVE_EXTENSIONS, TYPE_ZIP);

        // Favorite list
        getFavoriteFiles();

        mTaskInfo.mErrorCode = ERROR_CODE_SUCCESS;
        return mTaskInfo;
    }

    private void getFavoriteFiles(){
        FavoriteDBHandler dbHandler = new FavoriteDBHandler(mContext);
        Cursor cursor = dbHandler.queryFavoritePathCursor();
        if(null == cursor || 0 == cursor.getCount()){
            return;
        }

        cursor.moveToFirst();
        do{
            TaskInfo taskInfo = new TaskInfo();
            String path = cursor.getString(0);
            taskInfo.setFileInfo(new FileInfo(mContext, path));
            taskInfo.mErrorCode = TYPE_FAVORITE;
            publishProgress(taskInfo);
        }while(cursor.moveToNext());
        cursor.close();
    }

    private void getMediaFiles(String[] extensions, int type) {
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = new String[]{MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.SIZE};
        String selection = "(";
        for(int i = 0; i < extensions.length; i++){
            if(0 != i) {
                selection = selection + " OR ";
            }
            selection = selection + MediaStore.Files.FileColumns.DATA + " LIKE '%" + extensions[i] + "'";
        }

        // 过滤太小的文件
        switch (type){
            case TYPE_IMAGE:
                selection = selection + ") AND " + MediaStore.Files.FileColumns.SIZE + " > " + IMAGE_MIN_SIZE;
                break;
            case TYPE_MUSIC:
                selection = selection + ") AND " + MediaStore.Files.FileColumns.SIZE + " > " + MUSIC_MIN_SIZE;
                break;
            case TYPE_VIDEO:
                selection = selection + ") AND " + MediaStore.Files.FileColumns.SIZE + " > " + VIDEO_MIN_SIZE;
                break;
            case TYPE_DOCUMENT:
                selection = selection + ") AND " + MediaStore.Files.FileColumns.SIZE + " > " + DOCUMENT_MIN_SIZE;
                break;
            case TYPE_ZIP:
                selection = selection + ") AND " + MediaStore.Files.FileColumns.SIZE + " > " + ARCHIVE_MIN_SIZE;
                break;
            default:
                selection = selection + ")";
                break;
        }

        Cursor cursor = mContext.getContentResolver().query(uri, projection, selection, null, null);
        if(cursor == null || cursor.getCount() == 0) {
            return;
        }

        cursor.moveToFirst();
        do {
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.mErrorCode = type;
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
            taskInfo.setFileInfo(new FileInfo(mContext, path));
            publishProgress(taskInfo);
        } while(cursor.moveToNext());
        cursor.close();
    }

    private void getTencentFiles(String path, int type){
        String[] pathList = new File(path).list();
        if(null == pathList || pathList.length <= 0){
            return;
        }
        for (String curPath : pathList) {
            String fullPath = path + "/" + curPath;
            File file = new File(fullPath);
            if (file.isDirectory()) {
                getTencentFiles(fullPath, type);
            } else {
                TaskInfo taskInfo = new TaskInfo();
                taskInfo.mErrorCode = type;
                taskInfo.setFileInfo(new FileInfo(mContext, fullPath));
                publishProgress(taskInfo);
            }
        }
    }
}
