package com.file.filemanager.Task;

import com.file.filemanager.Service.FileOperatorListener;

import java.io.File;

/**
 * Created by huang on 2018/5/20.
 */

public class RenameTask extends BaseAsyncTask {

    private String mOldPath;
    private String mNewPath;
    private TaskInfo mTaskInfo;

    public RenameTask(String oldPath, String newPath, FileOperatorListener listener){
        super(listener);
        mOldPath = oldPath;
        mNewPath = newPath;
        mTaskInfo = new TaskInfo();
        mTaskInfo.mErrorCode = ERROR_CODE_SUCCESS;
        mTaskInfo.mErrorPath = mNewPath;
    }

    @Override
    protected TaskInfo doInBackground(Void... params) {
        File oldFile = new File(mOldPath);
        File newFile = new File(mNewPath);
        if(newFile.exists()){
            mTaskInfo.mErrorCode = ERROR_CODE_FILE_EXIST;
            return mTaskInfo;
        }

        if(!oldFile.renameTo(newFile)){
            mTaskInfo.mErrorCode = ERROR_CODE_RENAME_FAILED;
        }

        return mTaskInfo;
    }
}
