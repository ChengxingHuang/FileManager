package com.file.filemanager.Task;

import com.file.filemanager.Service.FileOperatorListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_FILE_NOT_EXIST;
import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_MKDIR_ERROR;
import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_SUCCESS;
import static com.file.filemanager.Service.FileOperatorListener.ERROR_CODE_USER_CANCEL;

/**
 * Created by huang on 2018/3/4.
 */

public class PasteTask extends BaseAsyncTask {

    private static final int BUFFER_SIZE = 2048 * 1024;

    private List<String> mCopySrcPaths;
    private String mCopyDstPath;
    private String mCurName;
    private TaskInfo mTaskInfo;

    public PasteTask(List<String> srcPaths, String dstPath, FileOperatorListener listener){
        super(listener);
        mCopySrcPaths = srcPaths;
        mCopyDstPath = dstPath;
        mTaskInfo = new TaskInfo();
    }

    @Override
    protected TaskInfo.ErrorInfo doInBackground(Void... params) {
        TaskInfo.ErrorInfo errorInfo = new TaskInfo.ErrorInfo();

        for(String copySrcPath : mCopySrcPaths) {
            errorInfo.mErrorPath = copySrcPath;
            File srcFile = new File(copySrcPath);

            if (!srcFile.exists()) {
                errorInfo.mErrorCode = ERROR_CODE_FILE_NOT_EXIST;
                break;
            }

            if (srcFile.isDirectory()) {
                //copy文件夹
                errorInfo = copyDirectory(copySrcPath, mCopyDstPath);
            } else {
                //copy文件
                String[] tmp = copySrcPath.split("/");
                mCurName = tmp[tmp.length - 1];
                File dstFile = new File(mCopyDstPath + "/" + mCurName);
                errorInfo = copyFile(srcFile, dstFile);
            }
        }

        return errorInfo;
    }

    private TaskInfo.ErrorInfo copyFile(File srcFile, File dstFile){
        TaskInfo.ErrorInfo errorInfo = new TaskInfo.ErrorInfo();
        FileInputStream ins = null;
        FileOutputStream fos = null;

        try {
            int readBuffer;
            byte[] buffer = new byte[BUFFER_SIZE];
            ins = new FileInputStream(srcFile);
            fos = new FileOutputStream(dstFile);

            while (-1 != (readBuffer = ins.read(buffer))) {
                //用户取消黏贴
                if(isCancelled()) {
                    errorInfo.mErrorCode = ERROR_CODE_USER_CANCEL;
                    errorInfo.mErrorPath = dstFile.toString();
                    return errorInfo;
                }
                fos.write(buffer, 0, readBuffer);

                //更新数据
                mTaskInfo.updateProgress(readBuffer);
                mTaskInfo.updateCurName(mCurName);
                if(mTaskInfo.needUpdate()) {
                    publishProgress(mTaskInfo);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
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
                e.printStackTrace();
            }
        }

        errorInfo.mErrorCode = ERROR_CODE_SUCCESS;
        return errorInfo;
    }

    private TaskInfo.ErrorInfo copyDirectory(String srcPath, String dstPath){
        TaskInfo.ErrorInfo errorInfo = new TaskInfo.ErrorInfo();
        File srcFile = new File(srcPath);
        File[] files = srcFile.listFiles();

        String dirTmp[] = srcPath.split("/");
        String dirName = dirTmp[dirTmp.length -1];
        dstPath = dstPath + "/" + dirName;
        File dstFolder = new File(dstPath);
        if(!dstFolder.mkdirs()) {
            errorInfo.mErrorPath = dstPath;
            errorInfo.mErrorCode = ERROR_CODE_MKDIR_ERROR;
            return errorInfo;
        }

        for(File file : files){
            String tmp[] = file.toString().split("/");
            mCurName = tmp[tmp.length -1];
            File dstFile = new File(dstPath + "/" + mCurName);
            if(file.isDirectory()){
                if(!dstFile.mkdirs()) {
                    errorInfo.mErrorCode = ERROR_CODE_MKDIR_ERROR;
                    break;
                }
                errorInfo = copyDirectory(file.toString(), dstFile.toString());
                //复制过程中发生错误，立刻退出
                if(errorInfo.mErrorCode != ERROR_CODE_SUCCESS){
                    break;
                }
            }else{
                errorInfo = copyFile(file, dstFile);
                //复制过程中发生错误，立刻退出
                if(errorInfo.mErrorCode != ERROR_CODE_SUCCESS){
                    break;
                }
            }
        }

        return errorInfo;
    }
}
