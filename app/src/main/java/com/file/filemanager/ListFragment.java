package com.file.filemanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.file.filemanager.Service.FileOperatorListener;
import com.file.filemanager.Task.TaskInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import static com.file.filemanager.Task.BaseAsyncTask.ERROR_CODE_EMPTY_FOLDER;
import static com.file.filemanager.Task.BaseAsyncTask.ERROR_CODE_SUCCESS;

public class ListFragment extends Fragment {

    public static final String TAG = "ListFragment";

    private List<FileInfo> mFileList = new ArrayList<>();
    private List<StorageInfo> mStorageList = new ArrayList<>();
    private List<LastPosition> mLastPositionList = new ArrayList<>();
    private List<MountStorageManager.MountStorage> mMountStorageList;

    private FileListAdapter mFileListAdapter;

    private MainActivity mMainActivity;
    private RecyclerView mRecyclerView;
    private TextView mEmptyText;

    private String mCurPath;
    private String mRootPath;
    private boolean mIsStorageList = false;
    private boolean mIsShowSearchList = false;

    public ListFragment() {
    }

    // 用来记录recycle view的位置
    class LastPosition{
        int mLastTopOffset;
        int mLastPosition;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainActivity = (MainActivity) getActivity();
        View v =  inflater.inflate(R.layout.fragment_list, container, false);

        mEmptyText = (TextView) v.findViewById(R.id.empty_tips);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.list_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mMainActivity));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new ListItemDecoration(mMainActivity, LinearLayoutManager.VERTICAL));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(recyclerView.getLayoutManager() != null) {
                    getPositionAndOffset();
                }
            }
        });

        mLastPositionList.clear();

        if(null != mMountStorageList)
            mMountStorageList.clear();
        mMountStorageList = MountStorageManager.getInstance().getMountStorageList();
        Log.d(TAG, "Storage Count = " + mMountStorageList.size());

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        //updateCurrentList();
        // TODO: 2017/12/4 设置完排序方式后mLastPositionList需要被清空
    }

    //记录RecyclerView的位置
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

    //显示存储个数
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

        StorageListAdapter storageListAdapter = new StorageListAdapter(mMainActivity, mStorageList, this);
        mRecyclerView.setAdapter(storageListAdapter);
    }

    //显示指定路径列表
    public void showAbsolutePathFiles(String absolutePath){
        mCurPath = absolutePath;
        mIsStorageList = false;
        updateCurrentList();

        LastPosition pos = new LastPosition();
        pos.mLastPosition = -1;
        pos.mLastTopOffset = -1;
        mLastPositionList.add(pos);
    }

    public void showRootPathList(String rootPath){
        showAbsolutePathFiles(rootPath);
        mRootPath = rootPath;
    }

    public void showRootPathList(){
        if(1 == mMountStorageList.size()){
            //显示file list
            showRootPathList(mMountStorageList.get(0).mPath);
        }else if(mMountStorageList.size() > 1){
            //显示storage list
            showStorageList();
        }else {
            //没有storage的情况
            Toast.makeText(mMainActivity, R.string.no_storage, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onBackPressed(){
        if(null != mCurPath && mCurPath.contains("/")) {
            if(!mIsShowSearchList)
                mCurPath = mCurPath.substring(0, mCurPath.lastIndexOf("/"));
            else
                mIsShowSearchList = false;

            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyText.setVisibility(View.GONE);

            if (mCurPath.length() >= mRootPath.length()) {
                updateCurrentList();
                mLastPositionList.remove(mLastPositionList.size() - 1);
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

    public void backToPreList(){
        if(null == mCurPath){
            mIsStorageList = true;
            mStorageList.clear();
            showStorageList();
        }else{
            updateCurrentList();
        }
    }

    public String getCurPath(){
        return mCurPath;
    }

    public void updateCurrentList(){
        mMainActivity.showFiles(mCurPath, new ShowFilesOperationListener());
    }

    public void updateSearchList(ArrayList<FileInfo> list){
        mFileList.clear();
        // TODO: 2017/12/24 mFileList = list不可行，此时mFileList指向不同的地址，无法更新数据。用for的方式效率较低 
        for(int i = 0; i < list.size(); i++){
            mFileList.add(list.get(i));
        }
        Collections.sort(mFileList, new FileInfo.NameComparator(mMainActivity));
        if(null != mFileListAdapter) {
            mFileListAdapter.notifyDataSetChanged();
        }else{
            mFileListAdapter = new FileListAdapter(mMainActivity, mFileList, this);
            mRecyclerView.setAdapter(mFileListAdapter);
        }

        LastPosition pos = new LastPosition();
        pos.mLastPosition = -1;
        pos.mLastTopOffset = -1;
        mLastPositionList.add(pos);

        mIsShowSearchList = true;
    }

    //显示目录操作
    class ShowFilesOperationListener implements FileOperatorListener {

        @Override
        public void onTaskPrepare() {

        }

        @Override
        public void onTaskProgress(TaskInfo progressInfo) {

        }

        @Override
        public void onTaskResult(TaskInfo taskInfo) {
            switch (taskInfo.mErrorCode){
                case ERROR_CODE_EMPTY_FOLDER:
                    mRecyclerView.setVisibility(View.GONE);
                    mEmptyText.setVisibility(View.VISIBLE);
                    mEmptyText.setText(mCurPath + " " + mMainActivity.getString(R.string.is_empty));

                    LastPosition pos = new LastPosition();
                    pos.mLastPosition = -1;
                    pos.mLastTopOffset = -1;
                    mLastPositionList.add(pos);
                    break;

                case ERROR_CODE_SUCCESS:
                    mFileList.clear();
                    for(FileInfo fileInfo : taskInfo.getFileInfoList()){
                        mFileList.add(fileInfo);
                    }

                    Collections.sort(mFileList, new FileInfo.NameComparator(mMainActivity));
                    scrollToPosition();

                    if(null != mFileListAdapter) {
                        mFileListAdapter.notifyDataSetChanged();
                    }else {
                        mFileListAdapter = new FileListAdapter(mMainActivity, mFileList, ListFragment.this);
                        mRecyclerView.setAdapter(mFileListAdapter);
                    }
                    break;
            }
        }
    }
}
