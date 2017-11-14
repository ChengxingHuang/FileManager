package com.file.filemanager;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SettingActivity extends AppCompatActivity {

    private ListView mSettingList;
    private SettingListAdapter mAdapter;
    private ArrayList<Map<String, Object>> mBeanData = new ArrayList<>();
    private int mSettingTitleResource[][] = {
            {R.string.ascending, R.string.ascending_subtitle},
            {R.string.show_hide_files, R.string.show_hide_files_subtitle},
            {R.string.filter_small_pictures, R.string.filter_small_pictures_subtitle},
            {R.string.directory_sort_mode, R.string.folder_on_top},
            {R.string.sort_by, R.string.name}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.setting_toolbar_main);
        setSupportActionBar(toolbar);
        //显示返回箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initBean();

        mSettingList = (ListView)findViewById(R.id.setting_list);
        mAdapter = new SettingListAdapter(LayoutInflater.from(SettingActivity.this));
        mSettingList.setAdapter(mAdapter);
        mSettingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(R.string.directory_sort_mode == mSettingTitleResource[position][0]){
                    showDirectorySortModeDialog();
                }else if(R.string.sort_by == mSettingTitleResource[position][0]){
                    showSortByDialog();
                }
            }
        });
    }

    private void initBean(){
        for(int i = 0; i < mSettingTitleResource.length; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("title", getResources().getString(mSettingTitleResource[i][0]));

            if(R.string.directory_sort_mode == mSettingTitleResource[i][0]){
                int directorSortModeValue = PreferenceUtils.getDirectorSortModeValue(SettingActivity.this);
                String title = getResources().getStringArray(R.array.directory_sort_mode)[directorSortModeValue];
                map.put("subTitle", title);
            }else if(R.string.sort_by == mSettingTitleResource[i][0]){
                int sortBy = PreferenceUtils.getSortByValue(SettingActivity.this);
                String title = getResources().getStringArray(R.array.sort_by)[sortBy];
                map.put("subTitle", title);
            }else {
                map.put("subTitle", getResources().getString(mSettingTitleResource[i][1]));
            }

            if (R.string.ascending == mSettingTitleResource[i][0]
                    || R.string.show_hide_files == mSettingTitleResource[i][0]
                    || R.string.filter_small_pictures == mSettingTitleResource[i][0]) {
                map.put("switch", true);
            } else {
                map.put("switch", false);
            }

            mBeanData.add(map);
        }
    }

    private void showSortByDialog(){
        int defaultSortBy = PreferenceUtils.getSortByValue(SettingActivity.this);
        String[] sortByString = getResources().getStringArray(R.array.sort_by);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        AlertDialog dialog = builder.create();
        builder.setTitle(R.string.sort_by);
        builder.setSingleChoiceItems(sortByString, defaultSortBy, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PreferenceUtils.setSortByValue(SettingActivity.this, which);
                mAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showDirectorySortModeDialog(){
        int defaultDirectorySortMode = PreferenceUtils.getDirectorSortModeValue(SettingActivity.this);
        String[] directorySortModeString = getResources().getStringArray(R.array.directory_sort_mode);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        AlertDialog dialog = builder.create();
        builder.setTitle(R.string.directory_sort_mode);
        builder.setSingleChoiceItems(directorySortModeString, defaultDirectorySortMode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PreferenceUtils.setDirectorSortModeValue(SettingActivity.this, which);
                mAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        builder.show();
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

    class SettingListAdapter extends BaseAdapter{
        private LayoutInflater mInflater;

        public SettingListAdapter(LayoutInflater inflater){
            mInflater = inflater;
        }

        @Override
        public int getCount() {
            return mBeanData.size();
        }

        @Override
        public Object getItem(int position) {
            return mBeanData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;

            if(null == convertView){
                convertView = mInflater.inflate(R.layout.setting_list_item, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.mTitle = (TextView) convertView.findViewById(R.id.title);
                viewHolder.mSubTitle = (TextView)convertView.findViewById(R.id.sub_title);
                viewHolder.mSwitchButton = (SwitchCompat)convertView.findViewById(R.id.switch_button);

                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Map<String, Object> map = mBeanData.get(position);
            viewHolder.mTitle.setText((String)map.get("title"));

            if(R.string.directory_sort_mode == mSettingTitleResource[position][0]){
                int sort = PreferenceUtils.getDirectorSortModeValue(SettingActivity.this);
                String subTitle = getResources().getStringArray(R.array.directory_sort_mode)[sort];
                viewHolder.mSubTitle.setText(subTitle);
            }else if(R.string.sort_by == mSettingTitleResource[position][0]){
                int sort = PreferenceUtils.getSortByValue(SettingActivity.this);
                String subTitle = getResources().getStringArray(R.array.sort_by)[sort];
                viewHolder.mSubTitle.setText(subTitle);
            }else {
                viewHolder.mSubTitle.setText((String) map.get("subTitle"));
            }

            if((boolean)map.get("switch")){
                if(R.string.ascending == mSettingTitleResource[position][0]){
                    viewHolder.mSwitchButton.setChecked(PreferenceUtils.getOrderTypeValue(SettingActivity.this));
                }else if(R.string.show_hide_files == mSettingTitleResource[position][0]){
                    viewHolder.mSwitchButton.setChecked(PreferenceUtils.getShowHideFileValue(SettingActivity.this));
                }else if(R.string.filter_small_pictures == mSettingTitleResource[position][0]){
                    viewHolder.mSwitchButton.setChecked(PreferenceUtils.getShowSmallPictureValue(SettingActivity.this));
                }
                viewHolder.mSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(R.string.ascending == mSettingTitleResource[position][0]){
                            PreferenceUtils.setOrderTypeValue(SettingActivity.this, isChecked);
                        }else if(R.string.show_hide_files == mSettingTitleResource[position][0]){
                            PreferenceUtils.setShowHideFileValue(SettingActivity.this, isChecked);
                        }else if(R.string.filter_small_pictures == mSettingTitleResource[position][0]){
                            PreferenceUtils.setShowSmallPictureValue(SettingActivity.this, isChecked);
                        }
                    }
                });
            }else{
                viewHolder.mSwitchButton.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }
    }

    class ViewHolder{
        public TextView mTitle;
        public TextView mSubTitle;
        public SwitchCompat mSwitchButton;
    }
}
