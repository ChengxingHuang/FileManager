package com.file.filemanager.Task;

import android.util.Log;

import com.file.filemanager.Service.FileOperatorListener;
import com.file.filemanager.Service.TaskProgressInfo;

import java.io.File;

import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_DELETE_FAIL;
import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_FILE_NOT_EXIST;
import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_NO_PERMISSION;
import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_SUCCESS;

/**
 * Created by huang on 2018/3/12.
 */

public class DeleteTask extends BaseAsyncTask {
    private static final String TAG = "DeleteTask";

    private String mDeletePath;

    public DeleteTask(String deletePath, FileOperatorListener listener){
        super(listener);
        mDeletePath = deletePath;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        File deleteFile = new File(mDeletePath);
        if(!deleteFile.exists()){
            return ERROR_CODE_FILE_NOT_EXIST;
        }

        if(deleteFile.isDirectory()){
            return deleteFolder(deleteFile);
        }else{
            if(!deleteFile.canWrite()){
                return ERROR_CODE_NO_PERMISSION;
            }

            if(!deleteFile.delete()){
                return ERROR_CODE_DELETE_FAIL;
            }
        }

        return ERROR_CODE_SUCCESS;
    }

    private int deleteFolder(File path){
        for (File file : path.listFiles()) {
            if (file.isFile()) {
                if(!file.delete()){
                    Log.d(TAG, "delete file fail, path = " + path.toString());
                    return ERROR_CODE_DELETE_FAIL;
                }
            }else {
                deleteFolder(file);
            }
        }
        // 删除目录本身
        if(!path.delete()){
            Log.d(TAG, "delete folder fail, path = " + path.toString());
            return ERROR_CODE_DELETE_FAIL;
        }
        return ERROR_CODE_SUCCESS;
    }
}
