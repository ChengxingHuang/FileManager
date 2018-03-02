package com.file.filemanager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.file.filemanager.Task.AssortTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class CategoryFragment extends Fragment implements View.OnClickListener {

    public static final int CATEGORY_GRID_COUNT = 9;
    public static final String KEY_ICON = "icon";
    public static final String KEY_TITLE = "title";
    public static final String KEY_COUNT = "count";

    private int[] mCategoryIcon = {R.drawable.category_icon_image, R.drawable.category_icon_music, R.drawable.category_icon_video,
                    R.drawable.category_icon_document, R.drawable.category_icon_apk, R.drawable.category_icon_zip,
                    R.drawable.category_icon_favorite, R.drawable.category_icon_qq, R.drawable.category_icon_wechat,};

    private int[] mCategoryTitle = {R.string.category_image, R.string.category_music, R.string.category_video,
                    R.string.category_document, R.string.category_apk, R.string.category_archive,
                     R.string.category_favorite, R.string.category_qq, R.string.category_wechat};

    private List<Map<String, Object>> mCategoryList = new ArrayList<Map<String, Object>>();

    private GridView mCategoryGrid;
    private RecyclerView mCategoryRecyclerView;
    private Button mPhoneChartButton;
    private Button mSDCardChartButton;
    private FileListAdapter mFileListAdapter;

    private ArrayList<FileInfo> mCurList = new ArrayList<FileInfo>();

    private AssortTask mAssortTask;

    private int mCurCategoryIndex = -1;
    private ArrayList<ArrayList<FileInfo>> mCategoryListManager = new ArrayList<ArrayList<FileInfo>>();
    private boolean mIsShowSearchList = false;
    private boolean mHasSetAdapter = false;

    public CategoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_category, container, false);

        mCategoryGrid = (GridView)v.findViewById(R.id.category_grid);
        mCategoryGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCurCategoryIndex = position;
                showCategoryList();
            }
        });

        mCategoryRecyclerView = (RecyclerView)v.findViewById(R.id.category_list);
        mCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCategoryRecyclerView.setHasFixedSize(true);
        mCategoryRecyclerView.addItemDecoration(new ListItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        mPhoneChartButton = (Button)v.findViewById(R.id.phone_chart);
        mPhoneChartButton.setOnClickListener(this);
        mSDCardChartButton = (Button)v.findViewById(R.id.sdcard_chart);
        mSDCardChartButton.setOnClickListener(this);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public void onClick(View v) {
        if(v == mPhoneChartButton){
            Intent intent = new Intent("android.intent.action.pie_chart_activity");
            intent.putExtra("TYPE", 0);
            startActivity(intent);
        }else if(v == mSDCardChartButton){
            Intent intent = new Intent("android.intent.action.pie_chart_activity");
            intent.putExtra("TYPE", 1);
            startActivity(intent);
        }
    }

    public ArrayList<FileInfo> getCurCategoryList(){
        if(-1 == mCurCategoryIndex){
            return null;
        }
        return mCategoryListManager.get(mCurCategoryIndex);
    }

    public void updateSearchList(ArrayList<FileInfo> list){
        mCategoryGrid.setVisibility(View.GONE);

        mCurList.clear();
        for (int i = 0; i < list.size(); i++) {
            mCurList.add(list.get(i));
        }

        mCategoryRecyclerView.setVisibility(View.VISIBLE);
        Collections.sort(mCurList, new FileInfo.NameComparator(getActivity()));
        if(null == mFileListAdapter){
            mFileListAdapter = new FileListAdapter(getActivity(), mCurList, null);
            mCategoryRecyclerView.setAdapter(mFileListAdapter);
        }else {
            mFileListAdapter.notifyDataSetChanged();
        }
        mIsShowSearchList = true;
    }

    public void backToPreList() {
        if (-1 == mCurCategoryIndex) {
            mCategoryGrid.setVisibility(View.VISIBLE);
        } else{
            showCategoryList();
        }
    }

    private void showCategoryList(){
        ArrayList<FileInfo> tmp = mCategoryListManager.get(mCurCategoryIndex);
        if(null != tmp && tmp.size() > 0){
            mCurList.clear();
            for(int i = 0; i < tmp.size(); i++){
                mCurList.add(tmp.get(i));
            }
            mCategoryRecyclerView.setVisibility(View.VISIBLE);
            Collections.sort(mCurList, new FileInfo.NameComparator(getActivity()));

            mFileListAdapter = new FileListAdapter(getActivity(), mCurList, null);
            mCategoryRecyclerView.setAdapter(mFileListAdapter);

            mCategoryGrid.setVisibility(View.GONE);
        }else{
            String[] category = getActivity().getResources().getStringArray(R.array.category);
            Toast.makeText(getActivity(), category[mCurCategoryIndex], Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onBackPressed(){
        if(-1 != mCurCategoryIndex && mIsShowSearchList){
            mIsShowSearchList = false;
            showCategoryList();
            return true;
        }
        if(mCurList.size() > 0){
            mCategoryGrid.setVisibility(View.VISIBLE);
            mCategoryRecyclerView.setVisibility(View.GONE);
            mCurList.clear();
            mCurCategoryIndex = -1;
            return true;
        }
        return false;
    }

    public void updateCurrentList(){
        if(null != mCurList && mCurList.size() > 0){
            Collections.sort(mCurList, new FileInfo.NameComparator(getActivity()));
            mFileListAdapter.notifyDataSetChanged();
        }
    }

    private void initData(){
        if(null != mAssortTask && mAssortTask.getStatus() == AsyncTask.Status.RUNNING) {
            mAssortTask.cancel(true);
        }

        mAssortTask = new AssortTask(getActivity());
        mAssortTask.setAssortFinish(new AssortTask.AssortFinish() {
            @Override
            public void assortDone() {
                mCategoryList.clear();
                for(int i = 0; i < CATEGORY_GRID_COUNT; i++){
                    Map<String, Object> map = new HashMap<>();
                    map.put(KEY_ICON, mCategoryIcon[i]);
                    map.put(KEY_TITLE, getActivity().getResources().getString(mCategoryTitle[i]));
                    map.put(KEY_COUNT, null == mCategoryListManager.get(i) ? 0 : mCategoryListManager.get(i).size());

                    mCategoryList.add(map);
                }

                // 由于多线程的问题，setAdapter需要放在这里，不然可能先setAdapter导致数据为空
                // TODO: 2018/1/18 不确定多次setAdapter是否会有问题，暂时用flag的方式 
                if(!mHasSetAdapter) {
                    mHasSetAdapter = true;
                    mCategoryGrid.setAdapter(new SimpleAdapter(getActivity(), mCategoryList, R.layout.item_category,
                            new String[]{KEY_ICON, KEY_TITLE, KEY_COUNT},
                            new int[]{R.id.category_image, R.id.category_title, R.id.category_count}));
                }
            }
        });
        mAssortTask.execute(mCategoryListManager);

        List<MountStorageManager.MountStorage> mountStorageList = MountStorageManager.getInstance().getMountStorageList();
        ListIterator<MountStorageManager.MountStorage> iterator = mountStorageList.listIterator();
        while(iterator.hasNext()){
            MountStorageManager.MountStorage mountStorage = iterator.next();
            if(!mountStorage.mRemovable) {
                mPhoneChartButton.setVisibility(View.VISIBLE);
                mPhoneChartButton.setText(mountStorage.mDescription + "\n"
                        + mountStorage.mTotalSpace + " "
                        + mountStorage.mAvailableSpace);
            }else{
                mSDCardChartButton.setVisibility(View.VISIBLE);
                mSDCardChartButton.setText(mountStorage.mDescription + "\n"
                        + mountStorage.mTotalSpace + " "
                        + mountStorage.mAvailableSpace);
            }
        }
    }
}
