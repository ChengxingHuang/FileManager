package com.file.filemanager.Task;

import com.file.filemanager.Service.FileOperatorListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by huang on 2018/3/4.
 */

public class PasteTask extends BaseAsyncTask {

    private static final int BUFFER_SIZE = 2048 * 1024;

    private List<String> mCopySrcPaths;
    private String mCopyDstPath;
    private String mCurName;
    private String mFileSuffix;
    private TaskInfo mTaskInfo;

    public PasteTask(List<String> srcPaths, String dstPath, FileOperatorListener listener){
        super(listener);
        mCopySrcPaths = srcPaths;
        mCopyDstPath = dstPath;
        mTaskInfo = new TaskInfo();
    }

    @Override
    protected TaskInfo doInBackground(Void... params) {
        for(String copySrcPath : mCopySrcPaths) {
            mTaskInfo.mErrorPath = copySrcPath;
            File srcFile = new File(copySrcPath);

            if (!srcFile.exists()) {
                mTaskInfo.mErrorCode = ERROR_CODE_FILE_NOT_EXIST;
                break;
            }

            if (srcFile.isDirectory()) {
                //copy文件夹
                copyDirectory(copySrcPath, mCopyDstPath);
            } else {
                //copy文件
                String[] tmp = copySrcPath.split("/");
                mCurName = tmp[tmp.length - 1];
                File dstFile = new File(mCopyDstPath + "/" + mCurName);
                mFileSuffix = mCurName.substring(mCurName.lastIndexOf("."), mCurName.length());

                //文件已经存在目录路径中
                if(dstFile.exists()){
                    //目标路径和源路径是一样的，在源文件名中增加(x)
                    String tmpPath = copySrcPath.substring(0, copySrcPath.lastIndexOf("/"));
                    if(tmpPath.equals(mCopyDstPath)){
                        dstFile = new File(mCopyDstPath + "/" + reName(0) + mFileSuffix);
                    }else{
                        //需要提示框，按照提示框操作
                    }
                }
                copyFile(srcFile, dstFile);
            }
        }

        return mTaskInfo;
    }

    private String reName(int index){
        String newName = mCurName.substring(0, mCurName.lastIndexOf(".")) + "(" + index + ")";
        File newFile = new File(mCopyDstPath + "/" + newName + mFileSuffix);
        if(newFile.exists()){
            return reName(index + 1);
        }
        return newName;
    }

    private void copyFile(File srcFile, File dstFile){
        FileInputStream ins = null;
        FileOutputStream fos = null;

        try {
            int readBuffer;
            byte[] buffer = new byte[BUFFER_SIZE];
            ins = new FileInputStream(srcFile);
            fos = new FileOutputStream(dstFile);
            mTaskInfo.mErrorCode = ERROR_CODE_SUCCESS;

            while (-1 != (readBuffer = ins.read(buffer))) {
                //用户取消黏贴
                if(isCancelled()) {
                    mTaskInfo.mErrorCode = ERROR_CODE_USER_CANCEL;
                    mTaskInfo.mErrorPath = dstFile.toString();
                    return;
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
    }

    private void copyDirectory(String srcPath, String dstPath){
        File srcFile = new File(srcPath);
        File[] files = srcFile.listFiles();

        String dirTmp[] = srcPath.split("/");
        String dirName = dirTmp[dirTmp.length -1];
        dstPath = dstPath + "/" + dirName;
        File dstFolder = new File(dstPath);
        if(!dstFolder.mkdirs()) {
            mTaskInfo.mErrorPath = dstPath;
            mTaskInfo.mErrorCode = ERROR_CODE_MKDIR_ERROR;
            return;
        }

        for(File file : files){
            String tmp[] = file.toString().split("/");
            mCurName = tmp[tmp.length -1];
            File dstFile = new File(dstPath + "/" + mCurName);
            if(file.isDirectory()){
                if(!dstFile.mkdirs()) {
                    mTaskInfo.mErrorCode = ERROR_CODE_MKDIR_ERROR;
                    break;
                }
                copyDirectory(file.toString(), dstFile.toString());
                //复制过程中发生错误，立刻退出
                if(mTaskInfo.mErrorCode != ERROR_CODE_SUCCESS){
                    break;
                }
            }else{
                copyFile(file, dstFile);
                //复制过程中发生错误，立刻退出
                if(mTaskInfo.mErrorCode != ERROR_CODE_SUCCESS){
                    break;
                }
            }
        }
    }
}
