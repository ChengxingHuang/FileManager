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
            getTencentFiles(mountStorageList.get(i).mPath + "/" + PATH_QQ, false);
            getTencentFiles(mountStorageList.get(i).mPath + "/" + PATH_QQ2, false);
            getTencentFiles(mountStorageList.get(i).mPath + "/" + PATH_WECHAT, true);
        }

        // Media type
        getMediaFiles(IMAGE_EXTENSIONS);
        getMediaFiles(MUSIC_EXTENSIONS);
        getMediaFiles(VIDEO_EXTENSIONS );
        getMediaFiles(DOCUMENT_EXTENSIONS);
        getMediaFiles(APK_EXTENSIONS);
        getMediaFiles(ARCHIVE_EXTENSIONS);

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

    private void getMediaFiles(String[] extensions) {
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = new String[]{MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.TITLE};
        String selection = "";
        for(int i = 0; i < extensions.length; i++){
            if(0 != i) {
                selection = selection + " OR ";
            }
            selection = selection + MediaStore.Files.FileColumns.DATA + " LIKE '%" + extensions[i] + "'";
        }

        Cursor cursor = mContext.getContentResolver().query(uri, projection, selection, null, null);
        if(cursor == null || cursor.getCount() == 0) {
            return;
        }

        cursor.moveToFirst();
        do {
            TaskInfo taskInfo = new TaskInfo();
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
            if(IMAGE_EXTENSIONS == extensions){
                taskInfo.mErrorCode = TYPE_IMAGE;
            }else if(MUSIC_EXTENSIONS == extensions){
                taskInfo.mErrorCode = TYPE_MUSIC;
            }else if(VIDEO_EXTENSIONS  == extensions){
                taskInfo.mErrorCode = TYPE_VIDEO;
            }else if(DOCUMENT_EXTENSIONS == extensions){
                taskInfo.mErrorCode = TYPE_DOCUMENT;
            }else if(APK_EXTENSIONS == extensions){
                taskInfo.mErrorCode = TYPE_APK;
            }else{
                taskInfo.mErrorCode = TYPE_ZIP;
            }
            taskInfo.setFileInfo(new FileInfo(mContext, path));
            publishProgress(taskInfo);
        } while(cursor.moveToNext());
        cursor.close();
    }

    private void getTencentFiles(String path, boolean isWeChat){
        String[] pathList = new File(path).list();
        if(null == pathList || pathList.length <= 0){
            return;
        }
        for (String curPath : pathList) {
            String fullPath = path + "/" + curPath;
            File file = new File(fullPath);
            if (file.isDirectory()) {
                getTencentFiles(fullPath, isWeChat);
            } else {
                FileInfo info = new FileInfo(mContext, fullPath);
                TaskInfo taskInfo = new TaskInfo();
                if(isWeChat)
                    taskInfo.mErrorCode = TYPE_WECHAT;
                else
                    taskInfo.mErrorCode = TYPE_QQ;
                taskInfo.setFileInfo(info);
                publishProgress(taskInfo);
            }
        }
    }
}
