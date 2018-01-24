package com.file.filemanager.Task;

import android.content.Context;
import android.os.AsyncTask;

import com.file.filemanager.FileInfo;
import com.file.filemanager.PreferenceUtils;

import java.io.File;
import java.util.List;

/**
 * Created by huang on 2018/1/22.
 */

public class ShowListTask extends AsyncTask<List, Void, Integer> {

    public static final int ERROR_CODE_SUCCESS = 0x00;
    public static final int ERROR_CODE_PATH_ERROR = 0x01;
    public static final int ERROR_CODE_CHILD_LIST_EMPTY = 0x03;

    private Context mContext;
    private String mPath;
    private ShowListFinish mShowListFinish;

    public ShowListTask(Context context, String path){
        mContext = context;
        mPath = path;
    }

    public interface ShowListFinish{
        void showNormalPath();
        void showEmptyPath();
        void enterWrongPath();
    }

    public void setShowListFinish(ShowListFinish showListFinish){
        mShowListFinish = showListFinish;
    }

    @Override
    protected Integer doInBackground(List... params) {
        if(null == mPath){
            return ERROR_CODE_PATH_ERROR;
        }

        String fileArray[] = new File(mPath).list();
        if(null != fileArray && fileArray.length > 0) {
            params[0].clear();

            for (int i = 0; i < fileArray.length; i++) {
                FileInfo fileInfo = new FileInfo(mContext, mPath + '/' + fileArray[i]);
                // 是否为隐藏文件
                boolean canShowHideFile = PreferenceUtils.getShowHideFileValue(mContext);
                if (!canShowHideFile && fileInfo.isHideFileType()) {
                    continue;
                }

                params[0].add(fileInfo);
            }
        }else{
            return ERROR_CODE_CHILD_LIST_EMPTY;
        }

        return ERROR_CODE_SUCCESS;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if(ERROR_CODE_SUCCESS == integer) {
            mShowListFinish.showNormalPath();
        }else if(ERROR_CODE_CHILD_LIST_EMPTY == integer){
            mShowListFinish.showEmptyPath();
        }else if(ERROR_CODE_PATH_ERROR == integer){
            mShowListFinish.enterWrongPath();
        }
    }
}
