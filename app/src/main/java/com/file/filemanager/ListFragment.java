package com.file.filemanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListFragment extends Fragment {

    private List<Map<String, Object>> mDataList;

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;

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
            }

            @Override
            public void onItemLongClick(View v, int position) {
                // TODO: 2017/7/21 弹出分享/复制/剪切/删除/重命名/详情/收藏菜单
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        
        return v;
    }

    // TODO: 2017/7/20 获取目录结构下所有文件信息 
    private void initData(){
        mDataList = new ArrayList<Map<String, Object>>();

        for(int i = 0; i < 50; i++){
            Map<String, Object> map = new HashMap<String, Object>();

            if(i % 2 == 0) {
                map.put("FileTypeImage", R.drawable.file_type_folder);
                map.put("FileName", "folder" + i);
            }else{
                map.put("FileTypeImage", R.drawable.file_type_document);
                map.put("FileName", "document" + i);
            }
            map.put("LastChangeDate", "2017-7-19");
            map.put("LastChangeTime", "10:40");
            map.put("IncludeFileCount", "" + i);

            mDataList.add(map);
        }
    }
}
