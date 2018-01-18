package com.file.filemanager;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.file.filemanager.Task.AssortTask;
import com.file.filemanager.Task.SearchTask;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryFragment extends Fragment implements OnChartValueSelectedListener {

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

    private int[] mChartParties = {R.string.category_image, R.string.category_music, R.string.category_video,
                    R.string.category_document, R.string.category_apk, R.string.category_others};

    private List<Map<String, Object>> mCategoryList = new ArrayList<Map<String, Object>>();

    private GridView mCategoryGrid;
    private PieChart mChart;
    private TextView mNoFileText;
    private RecyclerView mCategoryRecyclerView;
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

        mChart = (PieChart)v.findViewById(R.id.chart);
        initChart();

        mNoFileText = (TextView)v.findViewById(R.id.no_files_text);

        mCategoryRecyclerView = (RecyclerView)v.findViewById(R.id.category_list);
        mCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCategoryRecyclerView.setHasFixedSize(true);
        mCategoryRecyclerView.addItemDecoration(new ListItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        initData();
    }

    public ArrayList<FileInfo> getCurCategoryList(){
        if(-1 == mCurCategoryIndex){
            return null;
        }
        return mCategoryListManager.get(mCurCategoryIndex);
    }

    public void updateSearchList(ArrayList<FileInfo> list){
        hideGridAndChart();

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
            showGridAndChart();
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
        }else{
            String[] category = getActivity().getResources().getStringArray(R.array.category);
            mNoFileText.setVisibility(View.VISIBLE);
            mNoFileText.setText(category[mCurCategoryIndex]);
        }

        hideGridAndChart();

    }

    private void hideGridAndChart(){
        mCategoryGrid.setVisibility(View.GONE);
        mChart.setVisibility(View.GONE);
    }

    private void showGridAndChart(){
        mCategoryGrid.setVisibility(View.VISIBLE);
        mChart.setVisibility(View.VISIBLE);
    }

    public boolean onBackPressed(){
        if(-1 != mCurCategoryIndex && mIsShowSearchList){
            mIsShowSearchList = false;
            showCategoryList();
            return true;
        }
        if(mCurList.size() > 0 || mNoFileText.getVisibility() == View.VISIBLE){
            showGridAndChart();
            mNoFileText.setVisibility(View.GONE);
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

    private void initChart(){
        mChart.setUsePercentValues(true);
        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setDrawHoleEnabled(false);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        // add a selection listener
        mChart.setOnChartValueSelectedListener(this);

        setChartData(5, 100);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
    }

    private void setChartData(int count, float range) {
        float mult = range;
        ArrayList<Entry> yValues1 = new ArrayList<Entry>();

        for (int i = 0; i < count + 1; i++) {
            yValues1.add(new Entry((float) (Math.random() * mult) + mult / 5, i));
        }

        ArrayList<String> xValues = new ArrayList<String>();

        for (int i = 0; i < count + 1; i++)
            xValues.add(getActivity().getResources().getString(mChartParties[i % mChartParties.length]));

        PieDataSet dataSet = new PieDataSet(yValues1, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);

        PieData data = new PieData(xValues, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(13f);
        data.setValueTextColor(Color.BLACK);
        //data.setValueTypeface(tf);
        mChart.setData(data);
        mChart.setDescription("");

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    private void initData(){
        if(null != mAssortTask && mAssortTask.getStatus() == AsyncTask.Status.RUNNING) {
            mAssortTask.cancel(true);
        }

        mAssortTask = new AssortTask(getActivity());
        mAssortTask.setAssortFinish(new AssortTask.AssortFinish() {
            @Override
            public void assortDone() {
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
    }

    @Override
    public void onValueSelected(Entry entry, int i, Highlight highlight) {

    }

    @Override
    public void onNothingSelected() {

    }
}
