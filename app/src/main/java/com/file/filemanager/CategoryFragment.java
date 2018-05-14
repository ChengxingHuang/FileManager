package com.file.filemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.file.filemanager.Service.FileOperatorListener;
import com.file.filemanager.Task.TaskInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static com.file.filemanager.Task.BaseAsyncTask.ERROR_CODE_SUCCESS;

public class CategoryFragment extends Fragment implements View.OnClickListener {

    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_MUSIC = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_DOCUMENT = 3;
    public static final int TYPE_APK = 4;
    public static final int TYPE_ZIP = 5;
    public static final int TYPE_FAVORITE = 6;
    public static final int TYPE_QQ = 7;
    public static final int TYPE_WECHAT = 8;

    private static final int CATEGORY_GRID_COUNT = 9;
    private static final String KEY_ICON = "icon";
    private static final String KEY_TITLE = "title";
    private static final String KEY_COUNT = "count";

    private static final int[] CATEGORY_ICON = {R.drawable.category_icon_image, R.drawable.category_icon_music, R.drawable.category_icon_video,
                    R.drawable.category_icon_document, R.drawable.category_icon_apk, R.drawable.category_icon_zip,
                    R.drawable.category_icon_favorite, R.drawable.category_icon_qq, R.drawable.category_icon_wechat,};
    private static final int[] CATEGORY_TITLE = {R.string.category_image, R.string.category_music, R.string.category_video,
                    R.string.category_document, R.string.category_apk, R.string.category_archive,
                     R.string.category_favorite, R.string.category_qq, R.string.category_wechat};

    private GridView mCategoryGrid;
    private RecyclerView mCategoryRecyclerView;
    private Button mPhoneChartButton;
    private Button mSDCardChartButton;
    private FileListAdapter mFileListAdapter;
    private SimpleAdapter mSimpleAdapter;
    private MainActivity mMainActivity;

    private List<FileInfo> mCurList = new ArrayList<>();
    private List<Map<String, Object>> mCategoryInfoList = new ArrayList<>();
    private List<ArrayList<FileInfo>> mCategoryListManager = new ArrayList<>();

    private int mCurCategoryIndex = -1;
    private boolean mIsShowSearchList = false;

    public CategoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_category, container, false);
        initData();

        mSimpleAdapter = new SimpleAdapter(mMainActivity, mCategoryInfoList, R.layout.item_category,
                new String[]{KEY_ICON, KEY_TITLE, KEY_COUNT},
                new int[]{R.id.category_image, R.id.category_title, R.id.category_count});
        mCategoryGrid = (GridView)v.findViewById(R.id.category_grid);
        mCategoryGrid.setAdapter(mSimpleAdapter);
        mCategoryGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCurCategoryIndex = position;
                showCategoryList();
            }
        });

        mFileListAdapter = new FileListAdapter(mMainActivity, mCurList, null);
        mCategoryRecyclerView = (RecyclerView)v.findViewById(R.id.category_list);
        mCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(mMainActivity));
        mCategoryRecyclerView.setHasFixedSize(true);
        mCategoryRecyclerView.addItemDecoration(new ListItemDecoration(mMainActivity, LinearLayoutManager.VERTICAL));
        mCategoryRecyclerView.setAdapter(mFileListAdapter);

        mPhoneChartButton = (Button)v.findViewById(R.id.phone_chart);
        mPhoneChartButton.setOnClickListener(this);
        mSDCardChartButton = (Button)v.findViewById(R.id.sdcard_chart);
        mSDCardChartButton.setOnClickListener(this);

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

        return v;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent("android.intent.action.pie_chart_activity");
        if(v == mPhoneChartButton){
            intent.putExtra("TYPE", 0);
        }else if(v == mSDCardChartButton){
            intent.putExtra("TYPE", 1);
        }
        startActivity(intent);
    }

    public void showCategory(){
        mMainActivity.sortFiles(new CategoryOperationListener());
    }

    public void backToPreList() {
        if (-1 == mCurCategoryIndex) {
            mCategoryGrid.setVisibility(View.VISIBLE);
        } else{
            showCategoryList();
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

    private void initData(){
        mMainActivity = (MainActivity) getActivity();
        for(int i = 0; i < CATEGORY_GRID_COUNT; i++){
            Map<String, Object> map = new HashMap<>();
            map.put(KEY_ICON, CATEGORY_ICON[i]);
            map.put(KEY_TITLE, mMainActivity.getString(CATEGORY_TITLE[i]));
            map.put(KEY_COUNT, 0);
            mCategoryInfoList.add(map);

            mCategoryListManager.add(new ArrayList<FileInfo>());
        }
    }

    private void showCategoryList(){
        mCurList.clear();
        ArrayList<FileInfo> list = mCategoryListManager.get(mCurCategoryIndex);
        for(int i = 0; i < list.size(); i++){
            mCurList.add(list.get(i));
        }

        if(mCurList.size() > 0){
            mCategoryGrid.setVisibility(View.GONE);
            mCategoryRecyclerView.setVisibility(View.VISIBLE);

            Collections.sort(mCurList, new FileInfo.NameComparator(mMainActivity));
            mFileListAdapter.notifyDataSetChanged();
        }else{
            String[] category = mMainActivity.getResources().getStringArray(R.array.category);
            Toast.makeText(mMainActivity, category[mCurCategoryIndex], Toast.LENGTH_SHORT).show();
        }
    }

    class CategoryOperationListener implements FileOperatorListener {

        @Override
        public void onTaskPrepare() {
            for(int i = 0; i < mCategoryListManager.size(); i++)
                mCategoryListManager.get(i).clear();
        }

        @Override
        public void onTaskProgress(TaskInfo taskInfo) {
            mCategoryListManager.get(taskInfo.mErrorCode).add(taskInfo.getFileInfo());
        }

        @Override
        public void onTaskResult(TaskInfo taskInfo) {
            if (ERROR_CODE_SUCCESS == taskInfo.mErrorCode){
                for(int i = 0; i < CATEGORY_GRID_COUNT; i++)
                    mCategoryInfoList.get(i).put(KEY_COUNT, mCategoryListManager.get(i).size());
                mSimpleAdapter.notifyDataSetChanged();
            }
        }
    }
}
