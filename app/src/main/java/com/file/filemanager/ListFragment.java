package com.file.filemanager;

import android.content.Context;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListFragment extends Fragment {

    public static final String TAG = "ListFragment";
    private static final int UI_STACK_MAX_LENGTH = 20;

    private List<FileInfo> mFileList = new ArrayList<FileInfo>();
    private List<StorageInfo> mStorageList = new ArrayList<StorageInfo>();

    private Context mContext;
    private RecyclerView mRecyclerView;

    private FileListAdapter mFileListAdapter;
    private StorageListAdapter mStorageListAdapter;

    private String[] mStoragePaths;
    private String mStoragePath;
    private String[] mUiDataStackPath = new String[UI_STACK_MAX_LENGTH];
    private int mUiDataStackLength = 0;

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

        initStorageData();
        Log.d(TAG, "Storage Count = " + mStoragePaths.length);

        // 只有一个有效存储路径，直接显示list，否则显示选择手机和SD卡的list
        if(1 == mStoragePaths.length || 1 == mStorageList.size()) {
            mStoragePath = mStoragePaths[0];
            mUiDataStackPath[mUiDataStackLength] = mStoragePath;
            mUiDataStackLength++;
            setFileListAdapter();
        }else{
            setStorageListAdapter();
        }

        return v;
    }

    /*
     * 初始化指定路径mStoragePath下的文件列表
     */
    private void initFilesData(){
        mFileList.clear();
        String fileNameArray[] = new File(mStoragePath).list();

        if(null != fileNameArray) {
            for (int i = 0; i < fileNameArray.length; i++) {
                FileInfo fileInfo = new FileInfo(mStoragePath + '/' + fileNameArray[i]);
                // 是否为隐藏文件
                if(fileInfo.isHideFileType()){
                    continue;
                }

                mFileList.add(fileInfo);
            }
            Collections.sort(mFileList, new FileInfo.NameComparator(true, true));
        }
    }

    /*
     * 获取系统共有几个存储路径：手机/SD卡
     */
    private void initStorageData(){
        StorageManager storageManager = (StorageManager)mContext.getSystemService(Context.STORAGE_SERVICE);
        try {
            Method getVolumePaths = storageManager.getClass().getMethod("getVolumePaths");
            mStoragePaths = (String[]) getVolumePaths.invoke(storageManager);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for(int i = 0; i < mStoragePaths.length; i++){
            String[] files = new File(mStoragePaths[i]).list();
            // 目录下没有文件或者文件夹，跳过这一级，vivo x7测试会有otg目录，但是都是null类型的
            if(null == files || files.length < 1){
                continue;
            }

            int storageImage = 0;
            String storageName = null;
            String storageAvailable = Utils.getStorageAvailableSpace(mContext, mStoragePaths[i]);
            String storageTotal = Utils.getStorageTotalSpace(mContext, mStoragePaths[i]);

            if(0 == i){
                storageImage = R.drawable.storage_phone;
                storageName = mContext.getResources().getString(R.string.phone);
            }else{
                storageImage = R.drawable.storage_sdcard;
                storageName = mContext.getResources().getString(R.string.sdcard);
            }

            StorageInfo storageInfo = new StorageInfo(storageImage, storageName, storageAvailable, storageTotal);
            mStorageList.add(storageInfo);
        }
    }

    private void setFileListAdapter(){
        initFilesData();

        mFileListAdapter = new FileListAdapter(mContext, mFileList);
        mFileListAdapter.setOnItemClickLister(new FileListAdapter.OnItemClickLister() {
            @Override
            public void onItemClick(View v, int position) {
                FileInfo fileInfo = mFileList.get(position);
                if(fileInfo.isFolder()){
                    if(fileInfo.getChildFilesCount() > 0) {
                        mStoragePath = fileInfo.getFileAbsolutePath();
                        mUiDataStackPath[mUiDataStackLength] = mStoragePath;
                        mUiDataStackLength++;
                        initFilesData();
                        mFileListAdapter.notifyDataSetChanged();
                    }else{
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.empty_folder), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    // TODO: 2017/7/21 点击文件：打开或提示无法打开；点击文件夹：进入下一级目录
                    String mimeType = fileInfo.getMimeType(fileInfo.getFileAbsolutePath());
                }
            }

            @Override
            public void onItemLongClick(View v, int position) {
                // TODO: 2017/7/21 弹出分享/复制/剪切/删除/重命名/详情/收藏菜单
            }
        });

        mRecyclerView.setAdapter(mFileListAdapter);
    }

    private void setStorageListAdapter(){
        mStorageListAdapter = new StorageListAdapter(mContext, mStorageList);
        mStorageListAdapter.setOnItemClickLister(new StorageListAdapter.OnItemClickLister() {
            @Override
            public void onItemClick(View v, int position) {
                mStoragePath = mStoragePaths[position];
                mUiDataStackPath[mUiDataStackLength] = mStoragePath;
                mUiDataStackLength++;
                setFileListAdapter();
            }

            @Override
            public void onItemLongClick(View v, int position) {

            }
        });

        mRecyclerView.setAdapter(mStorageListAdapter);
    }

    public boolean updateList(){
        // TODO: 2017/8/1 List回退栈，这种方式效率低下，需要优化 
        if(mUiDataStackLength > 1) {
            mStoragePath = mUiDataStackPath[mUiDataStackLength - 2];
            Log.d(TAG, "current storage path = " + mStoragePath);
            mUiDataStackLength--;
            initFilesData();
            mFileListAdapter.notifyDataSetChanged();
            return true;
        }

        return false;
    }
}
