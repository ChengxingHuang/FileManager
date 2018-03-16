package com.file.filemanager;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AboutActivity extends AppCompatActivity {

    private ListView mSettingList;
    private AboutListAdapter mAdapter;
    private ArrayList<Map<String, Object>> mBeanData = new ArrayList<>();
    private int mAboutTitleResource[][] = {
            {R.string.version, R.string.v1_0_0},
            {R.string.developer, R.string.chengxing_huang},
            {R.string.git_report, R.string.git_issue},
            {R.string.mail_report, R.string.mail_address},
            {R.string.wechat_donate, R.string.wechat_donate_subtitle},
            {R.string.alipay_donate, R.string.alipay_donate_subtitle}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.about_toolbar_main);
        setSupportActionBar(toolbar);
        //显示返回箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initBean();

        mSettingList = (ListView)findViewById(R.id.about_list);
        mAdapter = new AboutListAdapter(LayoutInflater.from(AboutActivity.this));
        mSettingList.setAdapter(mAdapter);
        mSettingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(R.string.git_report == mAboutTitleResource[position][0]){
                    Intent intent= new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri url = Uri.parse("https://github.com/ChengxingHuang/FileManager/issues/");
                    intent.setData(url);
                    startActivity(intent);
                }else if(R.string.mail_report == mAboutTitleResource[position][0]){
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:chengxinghuang@126.com"));
                    startActivity(intent);
                }else if(R.string.wechat_donate == mAboutTitleResource[position][0]){
                    //showSortByDialog();
                }else if(R.string.alipay_donate == mAboutTitleResource[position][0]){
                    //showSortByDialog();
                }
            }
        });
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

    private void initBean(){
        for(int i = 0; i < mAboutTitleResource.length; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("title", getString(mAboutTitleResource[i][0]));
            map.put("subtitle", getString(mAboutTitleResource[i][1]));
            mBeanData.add(map);
        }
    }

    class AboutListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public AboutListAdapter(LayoutInflater inflater){
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
                convertView = mInflater.inflate(R.layout.about_list_item, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.mTitle = (TextView) convertView.findViewById(R.id.title);
                viewHolder.mSubTitle = (TextView)convertView.findViewById(R.id.sub_title);

                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Map<String, Object> map = mBeanData.get(position);
            viewHolder.mTitle.setText((String)map.get("title"));
            viewHolder.mSubTitle.setText((String)map.get("subtitle"));

            return convertView;
        }
    }

    class ViewHolder{
        public TextView mTitle;
        public TextView mSubTitle;
    }
}
