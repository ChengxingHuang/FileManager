package com.file.filemanager.Task;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by huang on 2018/3/4.
 */

public class PasteTask extends AsyncTask<String, Integer, Integer> {

    private static final String TAG = "PasteTask";

    private static final int ERROR_CODE_SUCCESS = 0x00;
    private static final int ERROR_CODE_PARAMS_ERROR = 0x01;
    private static final int ERROR_CODE_SRC_FILE_NOT_EXIST = 0x02;
    private static final int ERROR_CODE_EXCEPTION = 0x04;

    private static final int BUFFER_SIZE = 1024;

    //param[0]:srcPath
    //param[1]:dstPath
    @Override
    protected Integer doInBackground(String... params) {
        String srcPath = params[0];
        String dstPath = params[1];

        if(null == srcPath || null == dstPath){
            return ERROR_CODE_PARAMS_ERROR;
        }

        File srcFile = new File(srcPath);
        if(!srcFile.exists()){
            return ERROR_CODE_SRC_FILE_NOT_EXIST;
        }

        if(srcFile.isDirectory()){
            //copy文件夹
        }else{
            //copy文件
            String[] tmp = srcPath.split("/");
            File dstFile = new File(dstPath + "/" + tmp[tmp.length - 1]);
            copyFile(srcFile, dstFile);
        }

        return ERROR_CODE_SUCCESS;
    }

    private int copyFile(File srcFile, File dstFile){
        int ret = ERROR_CODE_SUCCESS;
        FileInputStream ins = null;
        FileOutputStream fos = null;
        try {
            ins = new FileInputStream(srcFile);
            fos = new FileOutputStream(dstFile);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bufferRead = 0;
            while (-1 != (bufferRead = ins.read(buffer))) {
                fos.write(buffer, 0, bufferRead);
            }
        } catch (Exception e){
            Log.d(TAG, "copyFile IOException 1!");
            e.printStackTrace();
            ret = ERROR_CODE_EXCEPTION;
        } finally {
            try {
                if(null != ins){
                    ins.close();
                }
                if (null != fos) {
                    fos.flush();
                    fos.getFD().sync();
                    fos.close();
                }
            } catch (Exception e){
                Log.d(TAG, "copyFile IOException 2!");
                e.printStackTrace();
                ret = ERROR_CODE_EXCEPTION;
            }
        }

        return ret;
    }

    private void copyDirectory(){

    }
}
