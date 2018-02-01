package com.file.filemanager;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

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

public class PieChartActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private PieChart mChart;
    private int[] mChartParties = {R.string.category_image, R.string.category_music, R.string.category_video,
            R.string.category_document, R.string.category_apk, R.string.category_archive,
            R.string.category_others, R.string.empty};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_chart);

        Toolbar toolbar = (Toolbar) findViewById(R.id.pie_chart_toolbar_main);
        setSupportActionBar(toolbar);
        //显示返回箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mChart = (PieChart)findViewById(R.id.pie_chart);
        initChart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //监听左上角的返回箭头
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onValueSelected(Entry entry, int i, Highlight highlight) {

    }

    @Override
    public void onNothingSelected() {

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

        setChartData(mChartParties.length - 1, 100);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
    }

    private void setChartData(int count, float range) {
        float mult = range;

        ArrayList<Entry> yValues1 = new ArrayList<Entry>();
        //for (int i = 0; i < count + 1; i++) {
        yValues1.add(new Entry(0.01f, 0));
        yValues1.add(new Entry(10, 1));
        yValues1.add(new Entry(10, 2));
        yValues1.add(new Entry(10, 3));
        yValues1.add(new Entry(10, 4));
        yValues1.add(new Entry(10, 5));
        yValues1.add(new Entry(10, 6));
        yValues1.add(new Entry(20, 7));
        //}

        ArrayList<String> xValues = new ArrayList<String>();
        for (int i = 0; i < count + 1; i++)
            xValues.add(getResources().getString(mChartParties[i % mChartParties.length]));

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

        mChart.setData(data);
        mChart.setDescription("");
        mChart.highlightValues(null);
        mChart.invalidate();
    }
}
