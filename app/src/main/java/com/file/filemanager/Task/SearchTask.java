package com.file.filemanager.Task;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.file.filemanager.FileInfo;
import com.file.filemanager.Service.FileOperatorListener;
import com.file.filemanager.Utils.PreferenceUtils;

/**
 * Created by huang on 2017/12/24.
 */

public class SearchTask extends BaseAsyncTask {

    private static final String TAG = "SearchTask";
    private final String mSearchName;
    private final String mSearchPath;
    private final ContentResolver mContentResolver;
    private Context mContext;
    private TaskInfo mTaskInfo;

    public SearchTask(Context context, String searchName, String searchPath, ContentResolver resolver, FileOperatorListener listener){
        super(listener);
        mSearchName = searchName;
        mSearchPath = searchPath;
        mContentResolver = resolver;
        mContext = context;
        mTaskInfo = new TaskInfo();
        mTaskInfo.mErrorCode = ERROR_CODE_SUCCESS;
    }

    @Override
    protected TaskInfo doInBackground(Void... voids) {
        /*
        MediaStore.Files.FileColumns.DATA：文件全称，包含路径和后缀
        MediaStore.Files.FileColumns.TITLE：文件名称，不含路径而且不包含后缀（包括文件夹），从模拟器看来是只截取.前面空格为止，比如a - a.jpg，只有存储a，而不是a - a
        MediaStore.Files.FileColumns.DISPLAY_NAME：文件名称，不含路径而且必须是包含后缀的文件（不包括文件夹），如果文件没有后缀，不会在这个字段里面
         */
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = { MediaStore.Files.FileColumns.DATA };
        String[] selectionArgs = { "%" + mSearchName + "%", "%" + mSearchPath + "%"};

        //判断是否需要全字匹配
        if(PreferenceUtils.getSearchWholeWordValue(mContext)){
            selectionArgs[0] = mSearchName;
        }

        // TODO: 2018/5/13 模糊匹配和%怎么用的？？？
        Cursor cursor = mContentResolver.query(uri, projection,
                MediaStore.Files.FileColumns.TITLE + " like ? and " + MediaStore.Files.FileColumns.DATA + " like ? ",
                selectionArgs, null);
        if (cursor == null) {
            mTaskInfo.mErrorCode = ERROR_CODE_CURSOR_NULL;
            return mTaskInfo;
        }else if(0 == cursor.getCount()){
            mTaskInfo.mErrorCode = ERROR_CODE_NO_MATCH_FILES;
            return mTaskInfo;
        }

        cursor.moveToFirst();
        try {
            while (!cursor.isAfterLast()) {
                if (isCancelled()) {
                    mTaskInfo.mErrorCode = ERROR_CODE_USER_CANCEL;
                    return mTaskInfo;
                }

                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                Log.d(TAG, "search path = " + path);
                mTaskInfo.setFileInfo(new FileInfo(mContext, path));
                publishProgress(mTaskInfo);
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return mTaskInfo;
    }
}
