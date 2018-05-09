package com.file.filemanager.Task;

import com.file.filemanager.FileInfo;

import java.util.List;

/**
 * Created by huang on 2018/4/1.
 */

public class TaskInfo {
    private static final int NEED_UPDATE_TIME = 200;
    private long mStartOperationTime = 0;
    private long mProgressSize = 0;
    private String mCurName;

    public int mErrorCode;
    public String mErrorPath;
    public FileInfo mFileInfo;

    public TaskInfo() {
        mStartOperationTime = System.currentTimeMillis();
    }

    public void updateProgress(long addSize) {
        mProgressSize += addSize;
    }

    public long getProgress() {
        return mProgressSize;
    }

    public void updateCurName(String curName){
        mCurName = curName;
    }

    public String getCurName(){
        return mCurName;
    }

    //200m刷新一次
    public boolean needUpdate() {
        long operationTime = System.currentTimeMillis() - mStartOperationTime;
        if (operationTime > NEED_UPDATE_TIME) {
            mStartOperationTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public void setFileInfo(FileInfo fileInfo){
        mFileInfo = fileInfo;
    }

    public FileInfo getFileInfo(){
        return mFileInfo;
    }
}
