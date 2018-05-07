package com.file.filemanager.Task;

import com.file.filemanager.Service.FileOperatorListener;

import java.io.File;

import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_FILE_EXIST;
import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_MKDIR_ERROR;
import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_SUCCESS;

/**
 * Created by huang on 2018/1/22.
 */

public class CreateFolderTask extends BaseAsyncTask {
    private String mPath;

    public CreateFolderTask(String path, FileOperatorListener listener){
        super(listener);
        mPath = path;
    }

    @Override
    protected TaskInfo.ErrorInfo doInBackground(Void... params) {
        TaskInfo.ErrorInfo errorInfo = new TaskInfo.ErrorInfo();
        errorInfo.mErrorPath = mPath;

        File file = new File(mPath);
        if(file.exists() && file.isDirectory()){
            errorInfo.mErrorCode = ERROR_CODE_FILE_EXIST;
        } else {
            if (file.mkdirs())
                errorInfo.mErrorCode = ERROR_CODE_SUCCESS;
            else
                errorInfo.mErrorCode = ERROR_CODE_MKDIR_ERROR;
        }
        return errorInfo;
    }
}
