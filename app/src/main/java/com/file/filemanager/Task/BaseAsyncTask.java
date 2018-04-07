package com.file.filemanager.Task;

import android.os.AsyncTask;

import com.file.filemanager.Service.FileOperatorListener;
import com.file.filemanager.Service.TaskProgressInfo;

import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_USER_CANCEL;

/**
 * Created by huang on 2018/4/1.
 */

public abstract class BaseAsyncTask extends AsyncTask<Void, TaskProgressInfo, Integer> {

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
    protected void onProgressUpdate(TaskProgressInfo... values) {
        super.onProgressUpdate(values);
        if(null != values && null != values[0] && null != mListener){
            mListener.onTaskProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        if(null != mListener){
            mListener.onTaskResult(integer);
        }
    }

    @Override
    protected void onCancelled(Integer integer) {
        super.onCancelled(integer);
        if(null != mListener){
            mListener.onTaskResult(ERROR_CODE_USER_CANCEL);
            mListener = null;
        }
    }
}
