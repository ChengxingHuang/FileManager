package com.file.filemanager.Task;

import android.os.AsyncTask;

import com.file.filemanager.Service.FileOperatorListener;

import java.util.List;

/**
 * Created by huang on 2018/4/1.
 */

public abstract class BaseAsyncTask extends AsyncTask<Void, TaskInfo, TaskInfo> {

    public static final int ERROR_CODE_SUCCESS = 0x00;
    public static final int ERROR_CODE_USER_CANCEL = 0x01;
    public static final int ERROR_CODE_FILE_NOT_EXIST = 0x02;
    public static final int ERROR_CODE_DELETE_FAIL = 0x03;
    public static final int ERROR_CODE_DELETE_NO_PERMISSION = 0x04;
    public static final int ERROR_CODE_MKDIR_ERROR = 0x07;
    public static final int ERROR_CODE_FILE_EXIST = 0x08;
    public static final int ERROR_CODE_EMPTY_FOLDER = 0x09;
    public static final int ERROR_CODE_CURSOR_NULL = 0x10;
    public static final int ERROR_CODE_NO_MATCH_FILES = 0x11;
    public static final int ERROR_CODE_NO_ENOUGH_SPACE = 0x12;
    public static final int ERROR_CODE_RENAME_FAILED = 0x13;

    private FileOperatorListener mListener;

    public BaseAsyncTask(FileOperatorListener listener){
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        if(null != mListener){
            mListener.onTaskPrepare();
        }
    }

    @Override
    protected void onProgressUpdate(TaskInfo... values) {
        if(null != values && null != values[0] && null != mListener){
            mListener.onTaskProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(TaskInfo taskInfo) {
        if(null != mListener){
            mListener.onTaskResult(taskInfo);
        }
    }

    @Override
    protected void onCancelled(TaskInfo taskInfo) {
        if(null != mListener){
            mListener.onTaskResult(taskInfo);
            mListener = null;
        }
    }
}
