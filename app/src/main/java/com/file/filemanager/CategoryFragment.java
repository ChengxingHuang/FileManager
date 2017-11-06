package com.file.filemanager;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryFragment extends Fragment implements OnChartValueSelectedListener {

    public static final int CATEGORY_GRID_COUNT = 9;
    public static final String KEY_ICON = "icon";
    public static final String KEY_TITLE = "title";
    public static final String KEY_COUNT = "count";

    private int[] mCategoryIcon = {R.drawable.category_icon_image, R.drawable.category_icon_music, R.drawable.category_icon_video,
                    R.drawable.category_icon_document, R.drawable.category_icon_qq, R.drawable.category_icon_wechat,
                    R.drawable.category_icon_apk, R.drawable.category_icon_favorite, R.drawable.category_icon_encryption};

    private int[] mCategoryTitle = {R.string.category_image, R.string.category_music, R.string.category_video,
                    R.string.category_document, R.string.category_qq, R.string.category_wechat,
                    R.string.category_apk, R.string.category_favorite, R.string.category_encryption};

    private int[] mChartParties = {R.string.category_image, R.string.category_music, R.string.category_video,
                    R.string.category_document, R.string.category_apk, R.string.category_others};

    private List<Map<String, Object>> mDataList = new ArrayList<Map<String, Object>>();

    private GridView mCategoryGrid;
    private PieChart mChart;

    public CategoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initData();

        View v = inflater.inflate(R.layout.fragment_category, container, false);

        mCategoryGrid = (GridView)v.findViewById(R.id.category_grid);
        mCategoryGrid.setAdapter(new SimpleAdapter(getActivity(), mDataList, R.layout.item_category,
                new String[]{KEY_ICON, KEY_TITLE, KEY_COUNT},
                new int[]{R.id.category_image, R.id.category_title, R.id.category_count}));
        mCategoryGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO: 2017/7/19 根据分类信息显示不同的信息，应该不需要这么case的
                switch (position){
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        break;
                    case 8:
                        break;
                    default:
                        break;
                }
            }
        });

        mChart = (PieChart)v.findViewById(R.id.chart);
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

        setData(5, 100);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        return v;
    }

    private void setData(int count, float range) {
        float mult = range;
        ArrayList<Entry> yValues1 = new ArrayList<Entry>();

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        for (int i = 0; i < count + 1; i++) {
            yValues1.add(new Entry((float) (Math.random() * mult) + mult / 5, i));
        }

        ArrayList<String> xValues = new ArrayList<String>();

        for (int i = 0; i < count + 1; i++)
            xValues.add(getActivity().getResources().getString(mChartParties[i % mChartParties.length]));

        PieDataSet dataSet = new PieDataSet(yValues1, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // add a lot of colors

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
        // TODO: 2017/7/18 获取分类的total个数
        int count = 0;
        for(int i = 0; i < CATEGORY_GRID_COUNT; i++){
            Map<String, Object> map = new HashMap<>();
            map.put(KEY_ICON, mCategoryIcon[i]);
            map.put(KEY_TITLE, getActivity().getResources().getString(mCategoryTitle[i]));
            map.put(KEY_COUNT, count);

            mDataList.add(map);
        }
    }

    @Override
    public void onValueSelected(Entry entry, int i, Highlight highlight) {

    }

    @Override
    public void onNothingSelected() {

    }
}
