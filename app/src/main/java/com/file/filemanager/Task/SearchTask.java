package com.file.filemanager.Task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.file.filemanager.FileInfo;
import com.file.filemanager.MountStorageManager;
import com.file.filemanager.Utils.PreferenceUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by huang on 2017/12/24.
 */

public class SearchTask extends AsyncTask<String, Void, Void> {

    public static final String TAG = "SearchTask";
    private Context mContext;
    private SearchTask.OnSearchFinish mOnSearchFinish;
    private ArrayList<FileInfo> mSearchSourceList;
    private ArrayList<FileInfo> mSearchResultList;

    public SearchTask(ArrayList<FileInfo> list, Context context){
        mSearchSourceList = list;
        mContext = context;
        mSearchResultList = new ArrayList<>();
    }

    /*
    param[0]:搜索路径
    param[1]:搜索关键字
     */
    @Override
    protected Void doInBackground(String[] params) {
        if((null == params[0] && null == mSearchSourceList)
                || PreferenceUtils.getSearchRootValue(mContext)){
            ArrayList<MountStorageManager.MountStorage> storageList = MountStorageManager.getInstance().getMountStorageList();
            for(int i = 0; i < storageList.size(); i++){
                String rootPath = storageList.get(i).mPath;
                Log.d(TAG, "Search root path = " + rootPath + ", keyWord = " + params[1]);
                searchByPath(rootPath, params[1]);
            }
        } else if(null != params[0] && null != params[1]){
            Log.d(TAG, "Search path = " + params[0] + ", keyWord = " + params[1]);
            searchByPath(params[0], params[1]);
        }else if(null != mSearchSourceList && null != params[1]) {
            Log.d(TAG, "Search list size = " + mSearchSourceList.size() + ", keyWord = " + params[1]);
            searchByList(params[1]);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        mOnSearchFinish.searchFinish(mSearchResultList);
    }

    public interface OnSearchFinish{
        void searchFinish(ArrayList<FileInfo> list);
    }

    public void setOnSearchFinish(SearchTask.OnSearchFinish onSearchFinish){
        mOnSearchFinish = onSearchFinish;
    }

    private void searchByPath(String path, String keyWord){
        if(null == path){
            Log.d(TAG, "Please check: searchByPath path is null");
            return;
        }

        FileInfo info = new FileInfo(mContext, path);
        File[] files = info.getFile().listFiles();
        for(File file : files){
            String[] tmp = file.toString().split("/");
            String fullName = tmp[tmp.length - 1];
            search(file.toString(), fullName, keyWord);

            if(file.isDirectory()){
                searchByPath(file.toString(), keyWord);
            }
        }
    }

    private void searchByList(String keyWord){
        for(int i = 0; i < mSearchSourceList.size(); i++){
            FileInfo info = mSearchSourceList.get(i);
            String fullName = info.getFileName();

            search(info.getFileAbsolutePath(), fullName, keyWord);
        }
    }

    private void search(String filePath, String fullName, String keyWord){
        //全字匹配：需要匹配aaa.jpg和aaa
        if(PreferenceUtils.getSearchWholeWordValue(mContext)) {
            String realName = "";
            if(fullName.contains(".")){
                int pointLastIndex = fullName.lastIndexOf(".");
                realName = fullName.substring(0, pointLastIndex);
            }
            if (keyWord.equals(realName) || keyWord.equals(fullName)) {
                FileInfo fileInfo = new FileInfo(mContext, filePath);
                mSearchResultList.add(fileInfo);
            }
        }else {
            //模糊匹配：名字中包含关键字即可
            if(fullName.contains(keyWord)){
                FileInfo fileInfo = new FileInfo(mContext, filePath);
                mSearchResultList.add(fileInfo);
            }
        }
    }
}
