package com.file.filemanager;

import android.graphics.Color;
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
    public static final String PATH_WECHAT = "tencent/MicroMsg";
    public static final String PATH_QQ = "tencent/QQfile_recv";

    private int[] mCategoryIcon = {R.drawable.category_icon_image, R.drawable.category_icon_music, R.drawable.category_icon_video,
                    R.drawable.category_icon_document, R.drawable.category_icon_apk, R.drawable.category_icon_zip,
                    R.drawable.category_icon_favorite, R.drawable.category_icon_qq, R.drawable.category_icon_wechat,};

    private int[] mCategoryTitle = {R.string.category_image, R.string.category_music, R.string.category_video,
                    R.string.category_document, R.string.category_apk, R.string.category_archive,
                     R.string.category_favorite, R.string.category_qq, R.string.category_wechat};

    private int[] mChartParties = {R.string.category_image, R.string.category_music, R.string.category_video,
                    R.string.category_document, R.string.category_apk, R.string.category_others};

    private String[] mPicture = {".jpg", ".png", ".jpeg", ".gif", ".bmp"};
    private String[] mMusic = {".mp3", ".wma", ".amr", ".aac", ".flac", ".ape", ".midi", ".ogg"};
    private String[] mVideo = {".mpeg", ".avi", ".mov", ".wmv", ".3gp", ".mkv", ".mp4", ".rmvb"};
    private String[] mDocument = {".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".pdf"};
    private String[] mApk = {".apk"};
    private String[] mArchive = {".zip", ".rar", ".tar", ".gz", ".7z"};

    private List<Map<String, Object>> mCategoryList = new ArrayList<Map<String, Object>>();

    private GridView mCategoryGrid;
    private PieChart mChart;
    private TextView mNoFileText;
    private RecyclerView mCategoryRecyclerView;
    private FileListAdapter mFileListAdapter;

    private ArrayList<FileInfo> mListTmp;
    private ArrayList<FileInfo> mPictureList;
    private ArrayList<FileInfo> mMusicList;
    private ArrayList<FileInfo> mVideoList;
    private ArrayList<FileInfo> mDocumentList;
    private ArrayList<FileInfo> mApkList;
    private ArrayList<FileInfo> mArchiveList;
    private ArrayList<FileInfo> mQQList;
    private ArrayList<FileInfo> mWechatList;

    public CategoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_category, container, false);

        mCategoryGrid = (GridView)v.findViewById(R.id.category_grid);
        mCategoryGrid.setAdapter(new SimpleAdapter(getActivity(), mCategoryList, R.layout.item_category,
                new String[]{KEY_ICON, KEY_TITLE, KEY_COUNT},
                new int[]{R.id.category_image, R.id.category_title, R.id.category_count}));
        mCategoryGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        showCategoryList(R.string.no_picture);
                        break;
                    case 1:
                        showCategoryList(R.string.no_music);
                        break;
                    case 2:
                        showCategoryList(R.string.no_video);
                        break;
                    case 3:
                        showCategoryList(R.string.no_document);
                        break;
                    case 4:
                        showCategoryList(R.string.no_apk);
                        break;
                    case 5:
                        showCategoryList(R.string.no_archive);
                        break;
                    case 6:
                        break;
                    case 7:
                        showCategoryList(R.string.no_qq);
                        break;
                    case 8:
                        showCategoryList(R.string.no_wechat);
                        break;
                    default:
                        break;
                }
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

    private void showCategoryList(int emptyText){
        if(R.string.no_picture == emptyText){
            mListTmp = mPictureList;
        }else if(R.string.no_music == emptyText){
            mListTmp = mMusicList;
        }else if(R.string.no_video == emptyText){
            mListTmp = mVideoList;
        }else if(R.string.no_document == emptyText){
            mListTmp = mDocumentList;
        }else if(R.string.no_apk == emptyText){
            mListTmp = mApkList;
        }else if(R.string.no_archive == emptyText){
            mListTmp = mArchiveList;
        }else if(R.string.no_wechat == emptyText){
            mListTmp = mWechatList;
        }else if(R.string.no_qq == emptyText){
            mListTmp = mQQList;
        }

        hideGridAndChart();
        if(null == mListTmp || 0 == mListTmp.size()){
            mNoFileText.setVisibility(View.VISIBLE);
            mNoFileText.setText(emptyText);
        }else{
            mCategoryRecyclerView.setVisibility(View.VISIBLE);
            Collections.sort(mListTmp, new FileInfo.NameComparator(getActivity()));
        }

        mFileListAdapter = new FileListAdapter(getActivity(), mListTmp, null);
        mCategoryRecyclerView.setAdapter(mFileListAdapter);
    }

    private void getTencentFiles(String path, boolean isWeChat){
        if(null != path){
            File[] fileArray = new File(path).listFiles();
            if(null != fileArray) {
                for (int i = 0; i < fileArray.length; i++) {
                    if (fileArray[i].isDirectory()) {
                        getTencentFiles(fileArray[i].toString(), isWeChat);
                    } else {
                        FileInfo info = new FileInfo(getActivity(), fileArray[i].toString());
                        if(isWeChat) {
                            if(!PreferenceUtils.getShowHideFileValue(getActivity())
                                && info.isHideFileType())
                                continue;
                            mWechatList.add(info);
                        }else{
                            if(!PreferenceUtils.getShowHideFileValue(getActivity())
                                    && info.isHideFileType())
                                continue;
                            mQQList.add(info);
                        }
                    }
                }
            }
        }
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
        if(null != mListTmp){
            showGridAndChart();
            mNoFileText.setVisibility(View.GONE);
            mCategoryRecyclerView.setVisibility(View.GONE);
            mListTmp = null;
            return true;
        }
        return false;
    }

    public void updateCurrentList(){
        if(null != mListTmp && mListTmp.size() > 0){
            Collections.sort(mListTmp, new FileInfo.NameComparator(getActivity()));
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
        String tencentRootPath = null;
        MountStorageManager storageManager = MountStorageManager.getInstance();
        ArrayList<MountStorageManager.MountStorage> mountStorageList = storageManager.getMountStorageList();
        if(1 == mountStorageList.size()){
            tencentRootPath = mountStorageList.get(0).mPath;
        }else{
            // TODO: 2017/12/17 有SD卡的情况
        }

        // TODO: 2017/12/17 需要用线程去处理
        mPictureList = Utils.getSpecificTypeOfFile(getActivity(), mPicture);
        mMusicList = Utils.getSpecificTypeOfFile(getActivity(), mMusic);
        mVideoList = Utils.getSpecificTypeOfFile(getActivity(), mVideo);
        mDocumentList = Utils.getSpecificTypeOfFile(getActivity(), mDocument);
        mApkList = Utils.getSpecificTypeOfFile(getActivity(), mApk);
        mArchiveList = Utils.getSpecificTypeOfFile(getActivity(), mArchive);
        mWechatList = new ArrayList<FileInfo>();
        mQQList = new ArrayList<FileInfo>();
        getTencentFiles(tencentRootPath + "/" + PATH_QQ, false);
        getTencentFiles(tencentRootPath + "/" + PATH_WECHAT, true);

        for(int i = 0; i < CATEGORY_GRID_COUNT; i++){
            int count = 0;
            Map<String, Object> map = new HashMap<>();
            map.put(KEY_ICON, mCategoryIcon[i]);
            map.put(KEY_TITLE, getActivity().getResources().getString(mCategoryTitle[i]));

            if(R.string.category_image == mCategoryTitle[i]){
                count = mPictureList.size();
            }else if(R.string.category_music == mCategoryTitle[i]){
                count = mMusicList.size();
            }else if(R.string.category_video == mCategoryTitle[i]){
                count = mVideoList.size();
            }else if(R.string.category_document == mCategoryTitle[i]){
                count = mDocumentList.size();
            }else if(R.string.category_apk == mCategoryTitle[i]){
                count = mApkList.size();
            }else if(R.string.category_archive == mCategoryTitle[i]){
                count = mArchiveList.size();
            }else if(R.string.category_qq == mCategoryTitle[i]){
                count = mQQList.size();
            }else if(R.string.category_wechat == mCategoryTitle[i]){
                count = mWechatList.size();
            }
            map.put(KEY_COUNT, count);

            mCategoryList.add(map);
        }
    }

    @Override
    public void onValueSelected(Entry entry, int i, Highlight highlight) {

    }

    @Override
    public void onNothingSelected() {

    }
}
