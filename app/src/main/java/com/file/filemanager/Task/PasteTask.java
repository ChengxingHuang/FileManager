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

    public static final int ERROR_CODE_SUCCESS = 0x00;

    private static final int ERROR_CODE_PARAMS_ERROR = 0x01;
    private static final int ERROR_CODE_SRC_FILE_NOT_EXIST = 0x02;
    private static final int ERROR_CODE_EXCEPTION = 0x04;
    private static final int ERROR_CODE_MKDIR_ERROR = 0x05;

    private static final int ERROR_CODE_DELETE_FILE_FAIL = 0x06;
    private static final int ERROR_CODE_DELETE_FOLDER_FAIL = 0x07;

    private static final int BUFFER_SIZE = 1024;

    private HandlePasteMessage mHandlePasteMsg;
    private boolean mIsCut = false;
    private String mSrcPath;
    private File mCurFilePath;

    public interface HandlePasteMessage{
        void updateProgress(int value);
        void pasteFinish(int errorCode);
    }

    // TODO: 2018/3/13 各种异常还没有进行测试

    public PasteTask(boolean isCut){
        mIsCut = isCut;
    }

    //param[0]:srcPath
    //param[1]:dstPath
    @Override
    protected Integer doInBackground(String... params) {
        mSrcPath = params[0];
        String dstPath = params[1];

        if(null == mSrcPath || null == dstPath){
            return ERROR_CODE_PARAMS_ERROR;
        }

        File srcFile = new File(mSrcPath);
        if(!srcFile.exists()){
            return ERROR_CODE_SRC_FILE_NOT_EXIST;
        }

        if(srcFile.isDirectory()){
            //copy文件夹
            return copyDirectory(mSrcPath, dstPath);
        }else{
            //copy文件
            String[] tmp = mSrcPath.split("/");
            File dstFile = new File(dstPath + "/" + tmp[tmp.length - 1]);
            return copyFile(srcFile, dstFile);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mHandlePasteMsg.updateProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Integer result) {
        int realResult = result;
        //黏贴文件失败，删除已经黏贴的部分
        if((null != mCurFilePath) && (ERROR_CODE_MKDIR_ERROR != result) && (ERROR_CODE_SUCCESS != result)){
            if(!mCurFilePath.delete()){
                realResult = ERROR_CODE_DELETE_FILE_FAIL;
            }
        }
        //剪切成功，删除原文件
        else if(mIsCut && (ERROR_CODE_SUCCESS == result)){
            File srcFile = new File(mSrcPath);
            if(srcFile.isDirectory()){
                realResult = deleteFolder(srcFile);
            }else{
                if(!srcFile.delete()){
                    realResult = ERROR_CODE_DELETE_FILE_FAIL;
                }
            }
        }

        mHandlePasteMsg.pasteFinish(realResult);
    }

    public void setPasteFinish(HandlePasteMessage pasteFinish){
        mHandlePasteMsg = pasteFinish;
    }

    private int copyFile(File srcFile, File dstFile){
        int ret = ERROR_CODE_SUCCESS;
        FileInputStream ins = null;
        FileOutputStream fos = null;
        mCurFilePath = dstFile;

        try {
            long readSize = 0;
            long srcFileSize = srcFile.length();
            ins = new FileInputStream(srcFile);
            fos = new FileOutputStream(dstFile);
            byte[] buffer = new byte[BUFFER_SIZE];
            int readBuffer;

            while (-1 != (readBuffer = ins.read(buffer))) {
                //用户取消黏贴，删除已经黏贴的部分
                if(isCancelled()) {
                    Log.d(TAG, "User cancel copy, delete " + mCurFilePath.toString());
                    if(!mCurFilePath.delete()){
                        Log.d(TAG, "User cancel copy, delete " + mCurFilePath.toString() + " failed!!!");
                        if(mCurFilePath.isDirectory())
                            ret = ERROR_CODE_DELETE_FOLDER_FAIL;
                        else
                            ret = ERROR_CODE_DELETE_FILE_FAIL;
                    }
                    break;
                }
                fos.write(buffer, 0, readBuffer);
                readSize += readBuffer;
                float percent = ((float)readSize / (float)srcFileSize) * 100;
                publishProgress(Math.round(percent));
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

    private int copyDirectory(String srcPath, String dstPath){
        int ret = ERROR_CODE_SUCCESS;
        File srcFile = new File(srcPath);
        File[] files = srcFile.listFiles();
        for(File file : files){
            String tmp[] = file.toString().split("/");
            String srcName = tmp[tmp.length -1];
            File dstFile = new File(dstPath + "/" + srcName);
            if(file.isDirectory()){
                if(!dstFile.mkdirs()) {
                    Log.d(TAG, "mkdir: " + dstFile.toString() + " failed!");
                    return ERROR_CODE_MKDIR_ERROR;
                }
                ret = copyDirectory(file.toString(), dstFile.toString());
                if(ERROR_CODE_SUCCESS != ret){
                    break;
                }
            }else{
                ret = copyFile(file, dstFile);
                if(ERROR_CODE_SUCCESS != ret){
                    break;
                }
            }
        }

        return ret;
    }

    private int deleteFolder(File path){
        if (path == null || !path.exists() || !path.isDirectory())
            return ERROR_CODE_PARAMS_ERROR;
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
        return ERROR_CODE_SUCCESS;
    }
}
