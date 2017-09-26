package com.file.filemanager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.MyViewHolder>{

    public static final String TAG = "FileListAdapter";

    private Context mContext;
    private List<FileInfo> mDataList;
    private LayoutInflater mInflater;
    private ListFragment mListFragment;

    public FileListAdapter(Context context, List<FileInfo> dataList, ListFragment fragment){
        mContext = context;
        mDataList = dataList;
        mInflater = LayoutInflater.from(mContext);
        mListFragment = fragment;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(mInflater.inflate(R.layout.file_list_item, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        FileInfo fileInfo = mDataList.get(position);
        holder.mFileTypeImage.setImageResource(fileInfo.getFileTypeImage());
        holder.mFileNameText.setText(fileInfo.getFileName());
        holder.mFileSizeText.setText(fileInfo.getFileSize());
        holder.mLastModifiedDateText.setText(fileInfo.getFileLastModifiedDate());
        holder.mLastModifiedTimeText.setText(fileInfo.getFileLastModifiedTime());
        holder.mOptionMenuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2017/7/21 点击option menu，弹出分享/复制/剪切/删除/重命名/详情/收藏菜单

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getLayoutPosition();
                FileInfo fileInfo = mDataList.get(position);
                if(fileInfo.isFolder()){
                    if(fileInfo.getChildFilesCount(mContext) > 0) {
                        //进入文件夹
                        Log.d(TAG, fileInfo.getFileAbsolutePath());
                        mListFragment.showFileList(fileInfo.getFileAbsolutePath());
                    }else{
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.empty_folder), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    String mimeType = fileInfo.getMimeType(fileInfo.getFileAbsolutePath());
                    Log.d(TAG, "mimeType = " + mimeType);
                    if(null != mimeType){
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(fileInfo.getFile()), mimeType);
                        mContext.startActivity(intent);
                    }else{
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.unknown_type), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getLayoutPosition();
                // TODO: 2017/7/21 弹出分享/复制/剪切/删除/重命名/详情/收藏菜单
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView mFileTypeImage;
        public TextView mFileNameText;
        public TextView mFileSizeText;
        public TextView mLastModifiedDateText;
        public TextView mLastModifiedTimeText;
        public ImageView mOptionMenuImage;

        public MyViewHolder(View view) {
            super(view);
            mFileTypeImage = (ImageView) view.findViewById(R.id.file_type_image);
            mFileNameText = (TextView) view.findViewById(R.id.file_name_text);
            mLastModifiedDateText = (TextView) view.findViewById(R.id.last_change_date_text);
            mLastModifiedTimeText = (TextView) view.findViewById(R.id.last_change_time_text);
            mFileSizeText = (TextView) view.findViewById(R.id.include_file_count_text);
            mOptionMenuImage = (ImageView) view.findViewById(R.id.option_menu_image);
        }
    }
}
