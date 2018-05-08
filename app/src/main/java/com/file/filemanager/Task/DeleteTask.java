package com.file.filemanager.Task;

import com.file.filemanager.Service.FileOperatorListener;

import java.io.File;
import java.util.List;

/**
 * Created by huang on 2018/3/12.
 */

public class DeleteTask extends BaseAsyncTask {
    private List<String> mDeletePaths;
    private TaskInfo mTaskInfo;

    public DeleteTask(List<String> deletePaths, FileOperatorListener listener){
        super(listener);
        mDeletePaths = deletePaths;
        mTaskInfo = new TaskInfo();
    }

    @Override
    protected TaskInfo doInBackground(Void... voids) {
        for(int i = 0; i < mDeletePaths.size(); i++) {
            String deletePath = mDeletePaths.get(i);
            File deleteFile = new File(deletePath);
            mTaskInfo.mErrorPath = deletePath;

            if (!deleteFile.exists()) {
                mTaskInfo.mErrorCode = ERROR_CODE_FILE_NOT_EXIST;
                return mTaskInfo;
            }

            if (deleteFile.isDirectory()) {
                deleteFolder(deleteFile);
            } else {
                if (!deleteFile.delete()) {
                    mTaskInfo.mErrorCode = ERROR_CODE_DELETE_FAIL;
                    return mTaskInfo;
                }
            }
        }

        mTaskInfo.mErrorCode = ERROR_CODE_SUCCESS;
        return mTaskInfo;
    }

    private void deleteFolder(File path){
        for (File file : path.listFiles()) {
            if (file.isFile()) {
                mTaskInfo.mErrorPath = path.toString();
                if (!file.canWrite()) {
                    mTaskInfo.mErrorCode = ERROR_CODE_DELETE_NO_PERMISSION;
                    return;
                }

                if(!file.delete()){
                    mTaskInfo.mErrorCode = ERROR_CODE_DELETE_FAIL;
                    return;
                }
            }else {
                deleteFolder(file);
            }
        }
        // 删除目录本身
        if(!path.delete()){
            mTaskInfo.mErrorPath = path.toString();
            mTaskInfo.mErrorCode = ERROR_CODE_DELETE_FAIL;
            return;
        }

        mTaskInfo.mErrorCode = ERROR_CODE_SUCCESS;
    }
}
