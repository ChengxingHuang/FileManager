package com.file.filemanager.Task;

import android.os.AsyncTask;

import com.file.filemanager.Service.FileOperatorListener;

import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_USER_CANCEL;

/**
 * Created by huang on 2018/4/1.
 */

public abstract class BaseAsyncTask extends AsyncTask<Void, TaskInfo, TaskInfo.ErrorInfo> {

    private FileOperatorListener mListener;

    public BaseAsyncTask(FileOperatorListener listener){
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(null != mListener){
            mListener.onTaskPrepare();
        }
    }

    @Override
    protected void onProgressUpdate(TaskInfo... values) {
        super.onProgressUpdate(values);
        if(null != values && null != values[0] && null != mListener){
            mListener.onTaskProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(TaskInfo.ErrorInfo errorInfo) {
        super.onPostExecute(errorInfo);
        if(null != mListener){
            mListener.onTaskResult(errorInfo);
        }
    }

    @Override
    protected void onCancelled(TaskInfo.ErrorInfo errorInfo) {
        super.onCancelled(errorInfo);
        if(null != mListener){
            mListener.onTaskResult(errorInfo);
            mListener = null;
        }
    }
}
