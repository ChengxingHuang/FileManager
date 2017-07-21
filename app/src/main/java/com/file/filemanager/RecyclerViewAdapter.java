package com.file.filemanager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{

    private Context mContext;
    private List<Map<String, Object>> mDataList;
    private LayoutInflater mInflater;
    public OnItemClickLister mOnItemClickLister;

    public RecyclerViewAdapter(Context context, List<Map<String, Object>> dataList){
        mContext = context;
        mDataList = dataList;
        mInflater = LayoutInflater.from(mContext);
    }

    public interface OnItemClickLister{
        void onItemClick(View v, int position);
        void onItemLongClick(View v, int position);
    }

    public void setOnItemClickLister(OnItemClickLister onItemClickLister){
        mOnItemClickLister = onItemClickLister;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(mInflater.inflate(R.layout.item_list, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Map<String, Object> map = mDataList.get(position);
        holder.mFileTypeImage.setImageResource((int)map.get("FileTypeImage"));
        holder.mFileNameText.setText((String)map.get("FileName"));
        holder.mLastChangeDateText.setText((String)map.get("LastChangeDate"));
        holder.mLastChangeTimeText.setText((String)map.get("LastChangeTime"));
        holder.mIncludeFilesCountText.setText((String)map.get("IncludeFileCount"));
        holder.mOptionMenuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2017/7/21 点击option menu，弹出分享/复制/剪切/删除/重命名/详情/收藏菜单

            }
        });

        //设置RecyclerView item的点击事件
        if(null != mOnItemClickLister){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    mOnItemClickLister.onItemClick(v, position);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = holder.getLayoutPosition();
                    mOnItemClickLister.onItemLongClick(v, position);

                    //如果return false执行完long click后还会再执行一次click
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView mFileTypeImage;
        public TextView mFileNameText;
        public TextView mLastChangeDateText;
        public TextView mLastChangeTimeText;
        public TextView mIncludeFilesCountText;
        public ImageView mOptionMenuImage;

        public MyViewHolder(View view) {
            super(view);
            mFileTypeImage = (ImageView) view.findViewById(R.id.file_type_image);
            mFileNameText = (TextView) view.findViewById(R.id.file_name_text);
            mLastChangeDateText = (TextView) view.findViewById(R.id.last_change_date_text);
            mLastChangeTimeText = (TextView) view.findViewById(R.id.last_change_time_text);
            mIncludeFilesCountText = (TextView) view.findViewById(R.id.include_file_count_text);
            mOptionMenuImage = (ImageView) view.findViewById(R.id.option_menu_image);
        }
    }
}
