package com.file.filemanager.Task;

import android.content.Context;
import android.os.AsyncTask;

import com.file.filemanager.FileInfo;
import com.file.filemanager.MainActivity;
import com.file.filemanager.MountStorageManager;
import com.file.filemanager.PreferenceUtils;
import com.file.filemanager.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huang on 2018/1/18.
 */

public class AssortTask extends AsyncTask<List, Void, Void> {

    public static final int CATEGORY_PICTURE = 0;
    public static final int CATEGORY_MUSIC = 1;
    public static final int CATEGORY_VIDEO = 2;
    public static final int CATEGORY_DOCUMENT = 3;
    public static final int CATEGORY_APK = 4;
    public static final int CATEGORY_ZIP = 5;
    public static final int CATEGORY_FAVORITE = 6;
    public static final int CATEGORY_QQ = 7;
    public static final int CATEGORY_WECHAT = 8;

    private String[] mPicture = {".jpg", ".png", ".jpeg", ".gif", ".bmp"};
    private String[] mMusic = {".mp3", ".wma", ".amr", ".aac", ".flac", ".ape", ".midi", ".ogg"};
    private String[] mVideo = {".mpeg", ".avi", ".mov", ".wmv", ".3gp", ".mkv", ".mp4", ".rmvb"};
    private String[] mDocument = {".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".pdf"};
    private String[] mApk = {".apk"};
    private String[] mArchive = {".zip", ".rar", ".tar", ".gz", ".7z"};

    public static final String PATH_WECHAT = "tencent/MicroMsg";
    public static final String PATH_QQ = "tencent/QQfile_recv";

    private MainActivity mMainActivity;
    private ArrayList<FileInfo> mQQList = new ArrayList<FileInfo>();;
    private ArrayList<FileInfo> mWechatList = new ArrayList<FileInfo>();;
    private AssortFinish mAssortFinish;

    public interface AssortFinish{
        void assortDone();
    }

    public AssortTask(Context context){
        mMainActivity = (MainActivity)context;
    }

    @Override
    protected Void doInBackground(List... params) {
        MountStorageManager storageManager = MountStorageManager.getInstance();
        ArrayList<MountStorageManager.MountStorage> mountStorageList = storageManager.getMountStorageList();
        for(int i = 0; i < mountStorageList.size(); i++){
            getTencentFiles(mountStorageList.get(i).mPath + "/" + PATH_QQ, false);
            getTencentFiles(mountStorageList.get(i).mPath + "/" + PATH_WECHAT, true);
        }
        params[0].add(CATEGORY_PICTURE, Utils.getSpecificTypeOfFile(mMainActivity, mPicture));
        params[0].add(CATEGORY_PICTURE, Utils.getSpecificTypeOfFile(mMainActivity, mPicture));
        params[0].add(CATEGORY_MUSIC, Utils.getSpecificTypeOfFile(mMainActivity, mMusic));
        params[0].add(CATEGORY_VIDEO, Utils.getSpecificTypeOfFile(mMainActivity, mVideo));
        params[0].add(CATEGORY_DOCUMENT, Utils.getSpecificTypeOfFile(mMainActivity, mDocument));
        params[0].add(CATEGORY_APK, Utils.getSpecificTypeOfFile(mMainActivity, mApk));
        params[0].add(CATEGORY_ZIP, Utils.getSpecificTypeOfFile(mMainActivity, mArchive));
        params[0].add(CATEGORY_FAVORITE, mMainActivity.getFavoriteList());
        params[0].add(CATEGORY_QQ, mQQList);
        params[0].add(CATEGORY_WECHAT, mWechatList);

        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        mAssortFinish.assortDone();
    }

    public void setAssortFinish(AssortFinish onAssortFinish){
        mAssortFinish = onAssortFinish;
    }

    private void getTencentFiles(String path, boolean isWeChat){
        if(null != path){
            File[] fileArray = new File(path).listFiles();
            if(null != fileArray) {
                for (int i = 0; i < fileArray.length; i++) {
                    if (fileArray[i].isDirectory()) {
                        getTencentFiles(fileArray[i].toString(), isWeChat);
                    } else {
                        FileInfo info = new FileInfo(mMainActivity, fileArray[i].toString());
                        if(isWeChat) {
                            if(!PreferenceUtils.getShowHideFileValue(mMainActivity) && info.isHideFileType())
                                continue;
                            mWechatList.add(info);
                        }else{
                            if(!PreferenceUtils.getShowHideFileValue(mMainActivity) && info.isHideFileType())
                                continue;
                            mQQList.add(info);
                        }
                    }
                }
            }
        }
    }
}
