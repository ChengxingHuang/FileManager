package com.file.filemanager.Task;

/**
 * Created by huang on 2018/4/1.
 */

public class TaskInfo {
    private static final int NEED_UPDATE_TIME = 200;
    private long mStartOperationTime = 0;
    private long mProgressSize = 0;
    private long mTotalSize = 0;
    private long mCurrentNumber = 0;
    private long mTotalNumber = 0;

    public TaskInfo() {
        mStartOperationTime = System.currentTimeMillis();
    }

    public long getProgress() {
        return mProgressSize;
    }

    public long getTotal() {
        return mTotalSize;
    }

    public long getCurrentNumber() {
        return mCurrentNumber;
    }

    public long getTotalNumber() {
        return mTotalNumber;
    }

    public void updateProgress(long addSize) {
        mProgressSize += addSize;
    }

    public void updateTotal(long addSize) {
        mTotalSize += addSize;
    }

    public void updateCurrentNumber(long addNumber) {
        mCurrentNumber += addNumber;
    }

    public void updateTotalNumber(long addNumber) {
        mTotalNumber += addNumber;
    }

    public boolean needUpdate() {
        long operationTime = System.currentTimeMillis() - mStartOperationTime;
        if (operationTime > NEED_UPDATE_TIME) {
            mStartOperationTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public static class ErrorInfo{
        public int mErrorCode;
        public String mErrorPath;
    }
}
