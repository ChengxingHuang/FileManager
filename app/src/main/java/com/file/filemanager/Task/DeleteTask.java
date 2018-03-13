package com.file.filemanager.Task;

import android.os.AsyncTask;

import java.io.File;

/**
 * Created by huang on 2018/3/12.
 */

public class DeleteTask extends AsyncTask<String, Integer, Integer> {

    public static final int ERROR_CODE_DELETE_SUCCESS = 0x00;
    private static final int ERROR_CODE_PARAMS_ERROR = 0x01;
    private static final int ERROR_CODE_DELETE_FILE_NOT_EXIST = 0x02;
    private static final int ERROR_CODE_DELETE_FILE_FAIL = 0x03;
    private static final int ERROR_CODE_DELETE_FOLDER_FAIL = 0x04;

    private HandlerDeleteMessage mHandleDeleteMsg;

    public interface HandlerDeleteMessage{
        void deleteFinish(int result);
    }

    public void setHandleDeleteMsg(HandlerDeleteMessage handleMsg){
        mHandleDeleteMsg = handleMsg;
    }

    //param[0]:delete path
    @Override
    protected Integer doInBackground(String... params) {
        String deletePath = params[0];

        if(null == deletePath){
            return ERROR_CODE_PARAMS_ERROR;
        }

        File deleteFile = new File(deletePath);
        if(!deleteFile.exists()){
            return ERROR_CODE_DELETE_FILE_NOT_EXIST;
        }

        if(deleteFile.isDirectory()){
            return deleteFolder(deleteFile);
        }else{
            if(!deleteFile.delete()){
                return ERROR_CODE_DELETE_FILE_FAIL;
            }
        }

        return ERROR_CODE_DELETE_SUCCESS;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        mHandleDeleteMsg.deleteFinish(integer);
    }

    private int deleteFolder(File path){
        if (path == null || !path.exists() || !path.isDirectory())
            return ERROR_CODE_DELETE_FOLDER_FAIL;
        for (File file : path.listFiles()) {
            if (file.isFile()) {
                if(!file.delete()){
                    return ERROR_CODE_DELETE_FILE_FAIL;
                }
            }else {
                deleteFolder(file);
            }
        }
        // 删除目录本身
        if(!path.delete()){
            return ERROR_CODE_DELETE_FOLDER_FAIL;
        }
        return ERROR_CODE_DELETE_SUCCESS;
    }
}
