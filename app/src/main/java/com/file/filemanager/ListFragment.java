package com.file.filemanager;

import android.content.Context;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListFragment extends Fragment {

    public static final String TAG = "ListFragment";

    private List<Map<String, Object>> mFileList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> mDocumentList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> mStorageList = new ArrayList<Map<String, Object>>();

    private RecyclerView mRecyclerView;

    private FileListAdapter mFileListAdapter;
    private StorageListAdapter mStorageListAdapter;

    private String[] mStoragePaths;
    private String mStoragePath;
    private int mFoldersCount = 0;
    private int mDocumentCount = 0;
    private String mFileName[];

    public ListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_list, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.list_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new ListItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        initStorageData();
        Log.d(TAG, "Storage Count = " + mStoragePaths.length);

        // 只有一个有效存储路径
        if(1 == mStoragePaths.length || 1 == mStorageList.size()) {
            mStoragePath = mStoragePaths[0];
            setFileListAdapter();
        }else{
            mStorageListAdapter = new StorageListAdapter(getActivity(), mStorageList);
            mStorageListAdapter.setOnItemClickLister(new StorageListAdapter.OnItemClickLister() {
                @Override
                public void onItemClick(View v, int position) {
                    mStoragePath = mStoragePaths[position];
                    setFileListAdapter();
                }

                @Override
                public void onItemLongClick(View v, int position) {

                }
            });

            mRecyclerView.setAdapter(mStorageListAdapter);
        }

        return v;
    }

    private void initFilesData(){
        mFileName = null;
        mFileList.clear();
        mDocumentList.clear();
        mFileName = new File(mStoragePath).list();

        if(null != mFileName) {
            for (int i = 0; i < mFileName.length; i++) {
                Map<String, Object> map = new HashMap<String, Object>();

                File fileTemp = new File(mStoragePath + '/' + mFileName[i]);
                int fileTypeImage = 0;
                String includeFileCount = null;

                if (fileTemp.isDirectory()) {
                    fileTypeImage = R.drawable.file_type_folder;
                    includeFileCount = FileUtils.getChildFolderFileCount(getActivity(), mStoragePath + '/' + mFileName[i]);
                } else if (fileTemp.isFile()) {
                    // TODO: 2017/7/21 根据MIME_TYPE，显示不同的图标
                    fileTypeImage = R.drawable.file_type_document;
                    includeFileCount = FileUtils.sizeToHumanString(fileTemp.length());
                }

                map.put("FileTypeImage", fileTypeImage);
                map.put("FileName", fileTemp.getName());
                map.put("LastChangeDate", FileUtils.getLastChangeDate(fileTemp));
                map.put("LastChangeTime", FileUtils.getLastChangeTime(fileTemp));
                map.put("IncludeFileCount", includeFileCount);

                if (fileTemp.isDirectory()) {
                    mFileList.add(map);
                } else if (fileTemp.isFile()) {
                    mDocumentList.add(map);
                }
            }

            mFoldersCount = mFileList.size();
            mDocumentCount = mDocumentList.size();

            // TODO: 2017/7/21 先显示文件夹再显示文件，这种方式比较渣，待优化
            for (int i = 0; i < mDocumentList.size(); i++) {
                mFileList.add(mDocumentList.get(i));
            }
        }
    }

    /*
     * 获取系统共有几个存储路径：手机/SD卡
     */
    private void initStorageData(){
        StorageManager storageManager = (StorageManager)getActivity().getSystemService(Context.STORAGE_SERVICE);
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

            Map<String, Object> map = new HashMap<String, Object>();

            if(0 == i){
                map.put("StorageTypeImage", R.drawable.storage_phone);
                map.put("StorageName", getActivity().getResources().getString(R.string.phone));
            }else{
                map.put("StorageTypeImage", R.drawable.storage_sdcard);
                map.put("StorageName", getActivity().getResources().getString(R.string.sdcard));
            }

            map.put("StorageAvailable", FileUtils.getStorageAvailableSpace(mStoragePaths[i]));
            map.put("StorageTotal", FileUtils.getStorageTotalSpace(mStoragePaths[i]));

            mStorageList.add(map);
        }
    }

    private void setFileListAdapter(){
        initFilesData();

        mFileListAdapter = new FileListAdapter(getActivity(), mFileList);
        mFileListAdapter.setOnItemClickLister(new FileListAdapter.OnItemClickLister() {
            @Override
            public void onItemClick(View v, int position) {
                // TODO: 2017/7/21 点击文件：打开或提示无法打开；点击文件夹：进入下一级目录
                if (position < mFoldersCount) {
                    mStoragePath = mStoragePath + '/' + mFileName[position];
                    initFilesData();
                    mFileListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onItemLongClick(View v, int position) {
                // TODO: 2017/7/21 弹出分享/复制/剪切/删除/重命名/详情/收藏菜单
            }
        });

        mRecyclerView.setAdapter(mFileListAdapter);
    }
}
