package com.file.filemanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.file.filemanager.Task.CreateFolderTask;
import com.file.filemanager.Task.ShowFilesTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class ListFragment extends Fragment {

    public static final String TAG = "ListFragment";

    private List<FileInfo> mFileList = new ArrayList<FileInfo>();
    private List<StorageInfo> mStorageList = new ArrayList<StorageInfo>();
    private List<LastPosition> mLastPositionList = new ArrayList<LastPosition>();
    private List<MountStorageManager.MountStorage> mMountStorageList;

    private FileListAdapter mFileListAdapter;
    private StorageListAdapter mStorageListAdapter;

    private MainActivity mMainActivity;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFloatingActionButton;
    private TextView mEmptyText;

    private String mCurPath;
    private String mRootPath;
    private boolean mIsStorageList = false;
    private boolean mIsShowSearchList = false;

    private CreateFolderTask mCreateFolderTask;
    private ShowFilesTask mShowFilesTask;

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

        mFloatingActionButton = mMainActivity.getFab();
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(mMainActivity);
                View view = inflater.inflate(R.layout.new_folder_dialog, null);
                final EditText nameEdit = (EditText)view.findViewById(R.id.name);

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mMainActivity, R.style.AlertDialogCustom));
                builder.setTitle(R.string.create_folder);
                builder.setView(view);
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        mkdir(mCurPath + File.separator + nameEdit.getEditableText().toString());
                    }
                });

                AlertDialog dialog = builder.create();
                //以下两句用来打开AlertDialog时直接打开软键盘
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();
            }
        });

        mLastPositionList.clear();

        if(null != mMountStorageList)
            mMountStorageList.clear();
        mMountStorageList = MountStorageManager.getInstance().getMountStorageList();
        Log.d(TAG, "Storage Count = " + mMountStorageList.size());

        if(1 == mMountStorageList.size()){
            //显示file list
            mRootPath = mMountStorageList.get(0).mPath;
            showAbsolutePathFiles(mRootPath);
        }else if(mMountStorageList.size() > 1){
            //显示storage list
            showStorageList();
        }else {
            // TODO: 2017/9/22 没有存储的情况 
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCurrentList();
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

    //创建文件夹
    private void mkdir(String path){
        if(null != mCreateFolderTask && mCreateFolderTask.getStatus() == AsyncTask.Status.RUNNING){
            mCreateFolderTask.cancel(true);
        }
        mCreateFolderTask = new CreateFolderTask(path);
        mCreateFolderTask.execute();
        mCreateFolderTask.setCreateFolderFinish(new CreateFolderTask.CreateFolderFinish() {
            @Override
            public void createFolderDone(int errorCode) {
                switch (errorCode){
                    case CreateFolderTask.ERROR_CODE_PARAM_ERROR:
                        Toast.makeText(mMainActivity, R.string.create_folder_illegal_tips, Toast.LENGTH_SHORT).show();
                        break;
                    case CreateFolderTask.ERROR_CODE_FOLDER_EXIST:
                        Toast.makeText(mMainActivity, R.string.create_folder_exist_tips, Toast.LENGTH_SHORT).show();
                        break;
                    case CreateFolderTask.ERROR_CODE_MKDIR_ERROR:
                        Toast.makeText(mMainActivity, R.string.create_folder_unknown_error_tips, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        updateCurrentList();
                        Toast.makeText(mMainActivity, R.string.create_folder_success_tips, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
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

        mStorageListAdapter = new StorageListAdapter(mMainActivity, mStorageList, this);
        mRecyclerView.setAdapter(mStorageListAdapter);
    }

    public void setFloatActionButtonVisibility(int visibility){
        mFloatingActionButton.setVisibility(visibility);
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
        if(null != mShowFilesTask && mShowFilesTask.getStatus() == AsyncTask.Status.RUNNING){
            mShowFilesTask.cancel(true);
        }

        mShowFilesTask = new ShowFilesTask(mMainActivity, mCurPath);
        mShowFilesTask.execute(mFileList);
        mShowFilesTask.setShowListFinish(new ShowFilesTask.ShowListFinish() {
            @Override
            public void showNormalPath() {
                Collections.sort(mFileList, new FileInfo.NameComparator(mMainActivity));
                scrollToPosition();

                if(null != mFileListAdapter) {
                    mFileListAdapter.notifyDataSetChanged();
                }else {
                    mFileListAdapter = new FileListAdapter(mMainActivity, mFileList, ListFragment.this);
                    mRecyclerView.setAdapter(mFileListAdapter);
                }
            }

            @Override
            public void showEmptyPath() {
                mRecyclerView.setVisibility(View.GONE);
                mEmptyText.setVisibility(View.VISIBLE);
                mEmptyText.setText(mCurPath + " " + mMainActivity.getString(R.string.is_empty));

                LastPosition pos = new LastPosition();
                pos.mLastPosition = -1;
                pos.mLastTopOffset = -1;
                mLastPositionList.add(pos);
            }

            @Override
            public void enterWrongPath() {

            }
        });
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
}
