package com.file.filemanager.Task;

import android.content.Context;

import com.file.filemanager.FileInfo;
import com.file.filemanager.Service.FileOperatorListener;
import com.file.filemanager.Utils.PreferenceUtils;

import java.io.File;

/**
 * Created by huang on 2018/1/22.
 */

public class ShowFilesTask extends BaseAsyncTask {
    private Context mContext;
    private String mPath;
    private TaskInfo mTaskInfo;

    public ShowFilesTask(Context context, String path, FileOperatorListener listener){
        super(listener);
        mContext = context;
        mPath = path;
        mTaskInfo = new TaskInfo();
    }

    @Override
    protected TaskInfo doInBackground(Void... voids) {
        String fileArray[] = new File(mPath).list();
        mTaskInfo.mErrorPath = mPath;
        mTaskInfo.mErrorCode = ERROR_CODE_SUCCESS;

        if(null != fileArray && fileArray.length > 0) {
            for(String path : fileArray){
                FileInfo fileInfo = new FileInfo(mContext, mPath + '/' + path);
                // 是否为隐藏文件
                boolean canShowHideFile = PreferenceUtils.getShowHideFileValue(mContext);
                if (!canShowHideFile && fileInfo.isHideFileType()) {
                    continue;
                }
                // TODO: 2018/5/13 这边用mTaskInfo会有Bug，没找到原因
                TaskInfo info = new TaskInfo();
                info.setFileInfo(fileInfo);
                publishProgress(info);
            }
        }else{
            mTaskInfo.mErrorCode = ERROR_CODE_EMPTY_FOLDER;
        }

        return mTaskInfo;
    }
}
