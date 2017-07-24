package com.file.filemanager;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListFragment extends Fragment {

    private List<Map<String, Object>> mDataList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> mFileList = new ArrayList<Map<String, Object>>();

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;

    private String mAbsolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private int mFoldersCount = 0;
    private int mFilesCount = 0;
    private String mFileName[];

    public ListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initData();

        View v = inflater.inflate(R.layout.fragment_list, container, false);

        mRecyclerView = (RecyclerView)v.findViewById(R.id.list_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new ListItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        mAdapter = new RecyclerViewAdapter(getActivity(), mDataList);
        mAdapter.setOnItemClickLister(new RecyclerViewAdapter.OnItemClickLister() {
            @Override
            public void onItemClick(View v, int position) {
                // TODO: 2017/7/21 点击文件：打开或提示无法打开；点击文件夹：进入下一级目录
                if(position < mFoldersCount){
                    mAbsolutePath = mAbsolutePath + '/' + mFileName[position];
                    initData();
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onItemLongClick(View v, int position) {
                // TODO: 2017/7/21 弹出分享/复制/剪切/删除/重命名/详情/收藏菜单
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        
        return v;
    }

    private void initData(){
        mFileName = null;
        mDataList.clear();
        mFileList.clear();
        mFileName = (new File(mAbsolutePath)).list();

        // TODO: 2017/7/24 获取系统文件的方式需要修改 
        for(int i = 0; i < mFileName.length; i++){
            Map<String, Object> map = new HashMap<String, Object>();

            File fileTemp = new File(mAbsolutePath + '/' + mFileName[i]);
            int fileTypeImage = 0;
            String includeFileCount = null;

            if(fileTemp.isDirectory()) {
                fileTypeImage = R.drawable.file_type_folder;
                includeFileCount = FileUtils.getChildFolderFileCount(getActivity(), mAbsolutePath + '/' + mFileName[i]);
            }else if(fileTemp.isFile()){
                // TODO: 2017/7/21 根据MIME_TYPE，显示不同的图标 
                fileTypeImage = R.drawable.file_type_document;
                includeFileCount = FileUtils.sizeToHumanString(fileTemp.length());
            }

            map.put("FileTypeImage", fileTypeImage);
            map.put("FileName", fileTemp.getName());
            map.put("LastChangeDate", FileUtils.getLastChangeDate(fileTemp));
            map.put("LastChangeTime", FileUtils.getLastChangeTime(fileTemp));
            map.put("IncludeFileCount", includeFileCount);

            if(fileTemp.isDirectory()) {
                mDataList.add(map);
            }else if(fileTemp.isFile()){
                mFileList.add(map);
            }
        }

        mFoldersCount = mDataList.size();
        mFilesCount = mFileList.size();

        // TODO: 2017/7/21 先显示文件夹再显示文件，这种方式比较渣，待优化
        for(int i = 0; i < mFileList.size(); i++) {
            mDataList.add(mFileList.get(i));
        }
    }
}
