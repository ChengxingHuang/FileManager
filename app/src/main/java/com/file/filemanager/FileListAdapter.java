package com.file.filemanager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.MyViewHolder> implements MainActivity.MainActivityCallBack{
    public static final String TAG = "FileListAdapter";

    private Context mContext;
    private List<FileInfo> mDataList;
    private LayoutInflater mInflater;
    private ListFragment mListFragment;
    private PopupMenu mPopupMenu;

    private int mCheckCount = 0;
    private boolean[] mIsPositionChecked;

    public FileListAdapter(Context context, List<FileInfo> dataList, ListFragment fragment){
        mContext = context;
        mDataList = dataList;
        mInflater = LayoutInflater.from(mContext);
        mListFragment = fragment;
        mIsPositionChecked = null;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(mInflater.inflate(R.layout.file_list_item, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final FileInfo fileInfo = mDataList.get(position);
        if(fileInfo.isChecked()){
            holder.mWholeView.setSelected(true);
        }else{
            holder.mWholeView.setSelected(false);
        }
        holder.mFileTypeImage.setImageDrawable(fileInfo.getFileTypeImage());
        holder.mFileNameText.setText(fileInfo.getFileName());
        holder.mFileSizeText.setText(fileInfo.getFileSize());
        holder.mLastModifiedDateText.setText(fileInfo.getFileLastModifiedDate());
        holder.mLastModifiedTimeText.setText(fileInfo.getFileLastModifiedTime());
        holder.mOptionMenuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(holder.mOptionMenuImage);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getLayoutPosition();

                if(mCheckCount > 0){
                    setCheckStatus(fileInfo, position);
                    notifyItemChanged(position);
                }else {
                    if (fileInfo.isFolder()) {
                        if (fileInfo.getChildFilesCount(mContext) > 0) {
                            //进入文件夹
                            Log.d(TAG, fileInfo.getFileAbsolutePath());
                            mListFragment.showFileList(fileInfo.getFileAbsolutePath());
                        } else {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.empty_folder), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String mimeType = fileInfo.getMimeType(fileInfo.getFileAbsolutePath());
                        Log.d(TAG, "mimeType = " + mimeType);
                        if (null != mimeType) {
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(fileInfo.getFile()), mimeType);
                            mContext.startActivity(intent);
                        } else {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.unknown_type), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getLayoutPosition();

                // TODO: 2017/10/25 放在构造函数中 mDataList.size() 为0？？？
                if(null == mIsPositionChecked){
                    mCheckCount = 0;
                    mIsPositionChecked = new boolean[mDataList.size()];
                    ((MainActivity) mListFragment.getActivity()).setCallBack(FileListAdapter.this);
                }

                setCheckStatus(fileInfo, position);
                notifyItemChanged(position);

                return true;
            }
        });
    }

    private void showPopupMenu(View anchor){
        mPopupMenu = new PopupMenu(mContext, anchor);
        mPopupMenu.getMenuInflater().inflate(R.menu.popup_menu_window, mPopupMenu.getMenu());
        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.copy:
                        Log.d("huangcx", "copy");
                        break;
                    case R.id.cut:
                        break;
                    case R.id.delete:
                        break;
                    case R.id.rename:
                        break;
                    case R.id.favorite:
                        break;
                    case R.id.encryption:
                        break;
                    case R.id.share:
                        break;
                    case R.id.detail:
                        break;
                }
                return true;
            }
        });
        mPopupMenu.show();
    }

    private void setCheckStatus(FileInfo fileInfo, int position){
        if(mIsPositionChecked[position]){
            //取消选中
            mIsPositionChecked[position] = false;
            fileInfo.setChecked(false);

            mCheckCount--;
            if(0 == mCheckCount){
                ((MainActivity) mListFragment.getActivity()).finishActionMode();
                mIsPositionChecked = null;
            }else {
                ((MainActivity) mListFragment.getActivity()).setItemCountView(mCheckCount + "");
            }
        }else{
            //选中
            mIsPositionChecked[position] = true;
            fileInfo.setChecked(true);

            mCheckCount++;
            if(1 == mCheckCount) {
                ((MainActivity) mListFragment.getActivity()).startActionMode();
                mListFragment.setFloatActionButtonVisibility(View.INVISIBLE);
            }
            ((MainActivity) mListFragment.getActivity()).setItemCountView(mCheckCount + "");
        }

        mDataList.set(position, fileInfo);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public void cleanCheck() {
        for(int i = 0; i < mDataList.size(); i++){
            FileInfo fileInfo = mDataList.get(i);
            if(fileInfo.isChecked()) {
                fileInfo.setChecked(false);
                mDataList.set(i, fileInfo);
            }
        }

        mIsPositionChecked = null;
        mListFragment.setFloatActionButtonVisibility(View.VISIBLE);
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public View mWholeView;
        public ImageView mFileTypeImage;
        public TextView mFileNameText;
        public TextView mFileSizeText;
        public TextView mLastModifiedDateText;
        public TextView mLastModifiedTimeText;
        public ImageView mOptionMenuImage;

        public MyViewHolder(View view) {
            super(view);
            mWholeView = (RelativeLayout) view.findViewById(R.id.file_list_item_view);
            mFileTypeImage = (ImageView) view.findViewById(R.id.file_type_image);
            mFileNameText = (TextView) view.findViewById(R.id.file_name_text);
            mLastModifiedDateText = (TextView) view.findViewById(R.id.last_change_date_text);
            mLastModifiedTimeText = (TextView) view.findViewById(R.id.last_change_time_text);
            mFileSizeText = (TextView) view.findViewById(R.id.include_file_count_text);
            mOptionMenuImage = (ImageView) view.findViewById(R.id.option_menu_image);
        }
    }
}
