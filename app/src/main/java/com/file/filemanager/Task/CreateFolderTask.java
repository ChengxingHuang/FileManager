package com.file.filemanager.Task;

import com.file.filemanager.Service.FileOperatorListener;

import java.io.File;

/**
 * Created by huang on 2018/1/22.
 */

public class CreateFolderTask extends BaseAsyncTask {
    private String mPath;
    private TaskInfo mTaskInfo;

    public CreateFolderTask(String path, FileOperatorListener listener){
        super(listener);
        mPath = path;
        mTaskInfo = new TaskInfo();
    }

    @Override
    protected TaskInfo doInBackground(Void... params) {
        mTaskInfo.mErrorPath = mPath;

        File file = new File(mPath);
        if(file.exists() && file.isDirectory()){
            mTaskInfo.mErrorCode = ERROR_CODE_FILE_EXIST;
        } else {
            if (file.mkdirs())
                mTaskInfo.mErrorCode = ERROR_CODE_SUCCESS;
            else
                mTaskInfo.mErrorCode = ERROR_CODE_MKDIR_ERROR;
        }
        return mTaskInfo;
    }
}
