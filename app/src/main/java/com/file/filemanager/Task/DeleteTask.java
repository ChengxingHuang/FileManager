package com.file.filemanager.Task;

import com.file.filemanager.Service.FileOperatorListener;

import java.io.File;
import java.util.List;

import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_DELETE_FAIL;
import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_FILE_NOT_EXIST;
import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_DELETE_NO_PERMISSION;
import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_SUCCESS;

/**
 * Created by huang on 2018/3/12.
 */

public class DeleteTask extends BaseAsyncTask {
    private List<String> mDeletePaths;

    public DeleteTask(List<String> deletePaths, FileOperatorListener listener){
        super(listener);
        mDeletePaths = deletePaths;
    }

    @Override
    protected TaskInfo.ErrorInfo doInBackground(Void... voids) {
        TaskInfo.ErrorInfo errorInfo = new TaskInfo.ErrorInfo();
        for(int i = 0; i < mDeletePaths.size(); i++) {
            String deletePath = mDeletePaths.get(i);
            File deleteFile = new File(deletePath);
            errorInfo.mErrorPath = deletePath;

            if (!deleteFile.exists()) {
                errorInfo.mErrorCode = ERROR_CODE_FILE_NOT_EXIST;
                return errorInfo;
            }

            if (deleteFile.isDirectory()) {
                return deleteFolder(deleteFile);
            } else {
                if (!deleteFile.delete()) {
                    errorInfo.mErrorCode = ERROR_CODE_DELETE_FAIL;
                    return errorInfo;
                }
            }
        }

        errorInfo.mErrorCode = ERROR_CODE_SUCCESS;
        return errorInfo;
    }

    private TaskInfo.ErrorInfo deleteFolder(File path){
        TaskInfo.ErrorInfo errorInfo = new TaskInfo.ErrorInfo();
        for (File file : path.listFiles()) {
            if (file.isFile()) {
                errorInfo.mErrorPath = path.toString();
                if (!file.canWrite()) {
                    errorInfo.mErrorCode = ERROR_CODE_DELETE_NO_PERMISSION;
                    return errorInfo;
                }

                if(!file.delete()){
                    errorInfo.mErrorCode = ERROR_CODE_DELETE_FAIL;
                    return errorInfo;
                }
            }else {
                deleteFolder(file);
            }
        }
        // 删除目录本身
        if(!path.delete()){
            errorInfo.mErrorPath = path.toString();
            errorInfo.mErrorCode = ERROR_CODE_DELETE_FAIL;
            return errorInfo;
        }

        errorInfo.mErrorCode = ERROR_CODE_SUCCESS;
        return errorInfo;
    }
}
