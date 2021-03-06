package com.file.filemanager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class StorageListAdapter extends RecyclerView.Adapter<StorageListAdapter.MyViewHolder> {

    private Context mContext;
    private List<StorageInfo> mDataList;
    private LayoutInflater mInflater;
    private ListFragment mListFragment;

    public StorageListAdapter(Context context, List<StorageInfo> dataList, ListFragment fragment){
        mContext = context;
        mDataList = dataList;
        mInflater = LayoutInflater.from(mContext);
        mListFragment = fragment;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(mInflater.inflate(R.layout.storage_list_item, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.mStorageTypeImage.setImageResource((int)mDataList.get(position).getStorageTypeImage());
        holder.mStorageNameText.setText(mDataList.get(position).getStorageName());
        holder.mStorageAvailableText.setText(mDataList.get(position).getStorageAvailable());
        holder.mStorageTotalText.setText(mDataList.get(position).getStorageTotal());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getLayoutPosition();
                mListFragment.showRootPathList(mDataList.get(position).getPath());
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getLayoutPosition();

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView mStorageTypeImage;
        public TextView mStorageNameText;
        public TextView mStorageAvailableText;
        public TextView mStorageTotalText;

        public MyViewHolder(View view) {
            super(view);
            mStorageTypeImage = (ImageView) view.findViewById(R.id.storage_type_image);
            mStorageNameText = (TextView) view.findViewById(R.id.storage_name_text);
            mStorageAvailableText = (TextView) view.findViewById(R.id.storage_available_text);
            mStorageTotalText = (TextView) view.findViewById(R.id.storage_total_text);
        }
    }
}
