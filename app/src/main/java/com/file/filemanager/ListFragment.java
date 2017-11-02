package com.file.filemanager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class ListFragment extends Fragment {

    public static final String TAG = "ListFragment";
    public static final int ERROR_CODE_SUCCESS = 0x00;
    public static final int ERROR_CODE_PATH_ERROR = 0x01;
    public static final int ERROR_CODE_CHILD_LIST_EMPTY = 0x03;

    private List<FileInfo> mFileList = new ArrayList<FileInfo>();
    private List<StorageInfo> mStorageList = new ArrayList<StorageInfo>();
    private FileListAdapter mFileListAdapter;
    private StorageListAdapter mStorageListAdapter;

    private Context mContext;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFloatingActionButton;

    private List<LastPosition> mLastPositionList = new ArrayList<LastPosition>();
    private int mCurPathLevel;

    private String mCurPath;
    private String mRootPath;
    private boolean mIsStorageList = false;
    private boolean mIsBackToPre = false;

    private ArrayList<MountStorageManager.MountStorage> mMountStorageList;

    public ListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        View v =  inflater.inflate(R.layout.fragment_list, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.list_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new ListItemDecoration(mContext, LinearLayoutManager.VERTICAL));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(recyclerView.getLayoutManager() != null) {
                    getPositionAndOffset();
                }
            }
        });

        mFloatingActionButton = (FloatingActionButton)v.findViewById(R.id.floatingActionButton);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2017/11/2 弹出新建文件夹窗口
                Log.d("huangcx", "mFloatingActionButton click");
            }
        });

        mLastPositionList.clear();
        mCurPathLevel = 0;

        MountStorageManager storageManager = MountStorageManager.getInstance();
        mMountStorageList = storageManager.getMountStorageList();
        Log.d(TAG, "Storage Count = " + mMountStorageList.size());

        if(1 == mMountStorageList.size()){
            //显示file list
            mFileListAdapter = new FileListAdapter(mContext, mFileList, this);
            mRecyclerView.setAdapter(mFileListAdapter);
            mRootPath = mMountStorageList.get(0).mPath;
            showFileList(mRootPath);
        }else if(mMountStorageList.size() > 1){
            //显示storage list
            showStorageList();
        }else {
            // TODO: 2017/9/22 没有存储的情况 
        }

        return v;
    }

    public void setFloatActionButtonVisibility(int visibility){
        mFloatingActionButton.setVisibility(visibility);
    }

    /**
     * 记录RecyclerView当前位置
     */
    private void getPositionAndOffset() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        View topView = layoutManager.getChildAt(0);

        if(topView != null) {
            LastPosition pos = new LastPosition();
            pos.mLastTopOffset = topView.getTop();
            pos.mLastPosition = layoutManager.getPosition(topView);

            mLastPositionList.set(mLastPositionList.size() - 1, pos);
        }
    }

    //还原RecyclerView的位置
    private void scrollToPosition() {
        if(mRecyclerView.getLayoutManager() != null
                && mLastPositionList.get(mLastPositionList.size() - 1).mLastPosition >= 0) {
            ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(
                    mLastPositionList.get(mLastPositionList.size() - 1).mLastPosition,
                    mLastPositionList.get(mLastPositionList.size() - 1).mLastTopOffset);
        }
    }

    class LastPosition{
        int mLastTopOffset;
        int mLastPosition;
    }

    public class ShowListAsyncTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            if(null == params || null == params[0]){
                return ERROR_CODE_PATH_ERROR;
            }

            String fileArray[] = new File(params[0]).list();

            if(null != fileArray && fileArray.length > 0) {
                mFileList.clear();
                mCurPath = params[0];

                for (int i = 0; i < fileArray.length; i++) {
                    FileInfo fileInfo = new FileInfo(mContext, params[0] + '/' + fileArray[i]);
                    // 是否为隐藏文件
                    boolean canShowHideFile = PreferenceUtils.canShowHideFile(mContext);
                    if (!canShowHideFile && fileInfo.isHideFileType()) {
                        continue;
                    }

                    mFileList.add(fileInfo);
                }
                //对文件列表进行排序
                boolean isFirstShowFolder = PreferenceUtils.isFirstShowFolder(mContext);
                boolean isAscending = PreferenceUtils.isAscending(mContext);
                Collections.sort(mFileList, new FileInfo.NameComparator(isFirstShowFolder, isAscending));
            }else{
                return ERROR_CODE_CHILD_LIST_EMPTY;
            }

            return ERROR_CODE_SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if(ERROR_CODE_SUCCESS == integer) {
                mFileListAdapter.notifyDataSetChanged();
                if(mIsBackToPre) {
                    scrollToPosition();
                    mIsBackToPre = false;
                }
            }else if(ERROR_CODE_CHILD_LIST_EMPTY == integer){
                // TODO: 2017/9/25 这样log要显示具体的路径
                Log.d(TAG, "this folder is empty");
            }else if(ERROR_CODE_PATH_ERROR == integer){
                // TODO: 2017/9/25 这样log要显示具体的路径
                Log.d(TAG, "enter a error path");
            }
        }
    }

    public void showFileList(String path){
        ShowListAsyncTask task = new ShowListAsyncTask();
        task.execute(path);

        if(mIsBackToPre){
            mCurPathLevel--;
            mLastPositionList.remove(mLastPositionList.size() - 1);
        }else{
            LastPosition pos = new LastPosition();
            pos.mLastPosition = -1;
            pos.mLastTopOffset = -1;
            mLastPositionList.add(pos);
            mCurPathLevel++;
        }
    }

    public void showRootPathList(String rootPath){
        mFileListAdapter = new FileListAdapter(mContext, mFileList, this);
        mRecyclerView.setAdapter(mFileListAdapter);
        mRootPath = rootPath;

        ShowListAsyncTask task = new ShowListAsyncTask();
        task.execute(mRootPath);
    }

    private void showStorageList(){
        ListIterator<MountStorageManager.MountStorage> iterator = mMountStorageList.listIterator();
        while(iterator.hasNext()){
            MountStorageManager.MountStorage mountStorage = iterator.next();
            // TODO: 2017/9/25 这边可能要做一些处理，直接通过removable进来判断，可能是错误的 
            if(!mountStorage.mRemovable) {
                mStorageList.add(new StorageInfo(R.drawable.storage_phone, mountStorage.mDescription,
                        mountStorage.mAvailableSpace, mountStorage.mTotalSpace, mountStorage.mPath));
            }else{
                mStorageList.add(new StorageInfo(R.drawable.storage_sdcard, mountStorage.mDescription,
                        mountStorage.mAvailableSpace, mountStorage.mTotalSpace, mountStorage.mPath));
            }
        }

        mStorageListAdapter = new StorageListAdapter(mContext, mStorageList, this);
        mRecyclerView.setAdapter(mStorageListAdapter);
    }

    public boolean backToPrePath(){
        if(null != mCurPath && mCurPath.contains("/")) {
            mCurPath = mCurPath.substring(0, mCurPath.lastIndexOf("/"));

            if (mCurPath.length() >= mRootPath.length()) {
                mIsBackToPre = true;
                showFileList(mCurPath);
                return true;
            } else if (mMountStorageList.size() > 1 && !mIsStorageList) {
                mIsStorageList = true;
                mStorageList.clear();
                showStorageList();
                return true;
            } else {
                mIsStorageList = false;
                return false;
            }
        }else{
            return false;
        }
    }
}
