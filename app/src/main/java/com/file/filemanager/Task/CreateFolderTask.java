package com.file.filemanager.Task;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;

/**
 * Created by huang on 2018/1/22.
 */

public class CreateFolderTask extends AsyncTask<Void, Void, Integer> {

    private static final int ERROR_CODE_CREATE_SUCCESS = 0x00;
    public static final int ERROR_CODE_PARAM_ERROR = 0x01;
    public static final int ERROR_CODE_FOLDER_EXIST = 0x02;
    public static final int ERROR_CODE_MKDIR_ERROR = 0x03;

    private String mPath;
    private CreateFolderFinish mCreateFolderFinish;

    public interface CreateFolderFinish{
        void createFolderDone(int errorCode);
    }

    public CreateFolderTask(String path){
        mPath = path;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Integer doInBackground(Void... params) {
        if(null == mPath || "".equals(mPath)){
            return ERROR_CODE_PARAM_ERROR;
        }

        File file = new File(mPath);
        if(file.exists() && file.isDirectory()){
            return ERROR_CODE_FOLDER_EXIST;
        }

        if(file.mkdirs())
            return ERROR_CODE_CREATE_SUCCESS;
        else
            return ERROR_CODE_MKDIR_ERROR;
    }

    @Override
    protected void onPostExecute(Integer result) {
        mCreateFolderFinish.createFolderDone(result);
    }

    public void setCreateFolderFinish(CreateFolderFinish createFolderFinish){
        mCreateFolderFinish = createFolderFinish;
    }
}
