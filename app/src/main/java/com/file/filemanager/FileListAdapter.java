package com.file.filemanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import com.file.filemanager.Task.DeleteTask;
import com.file.filemanager.Task.PasteTask;

import java.io.File;
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
                showPopupMenu(holder.mOptionMenuImage, fileInfo);
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
                        //进入文件夹
                        Log.d(TAG, fileInfo.getFileAbsolutePath());
                        mListFragment.showAbsolutePathFiles(fileInfo.getFileAbsolutePath());
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

    private void showPopupMenu(View anchor, final FileInfo fileInfo){
        mPopupMenu = new PopupMenu(mContext, anchor);
        mPopupMenu.getMenuInflater().inflate(R.menu.popup_menu_window, mPopupMenu.getMenu());
        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.copy:
                        ((MainActivity) mListFragment.getActivity()).setPasteIconVisible(true);
                        ((MainActivity) mListFragment.getActivity()).setCutMode(false);
                        ((MainActivity) mListFragment.getActivity()).setSrcPastePath(fileInfo.getFileAbsolutePath());
                        break;
                    case R.id.cut:
                        ((MainActivity) mListFragment.getActivity()).setPasteIconVisible(true);
                        ((MainActivity) mListFragment.getActivity()).setCutMode(true);
                        ((MainActivity) mListFragment.getActivity()).setSrcPastePath(fileInfo.getFileAbsolutePath());
                        break;
                    case R.id.delete:
                        DeleteTask task = new DeleteTask();
                        task.execute(fileInfo.getFileAbsolutePath());
                        task.setHandleDeleteMsg(new DeleteTask.HandlerDeleteMessage() {
                            @Override
                            public void deleteFinish(int result) {
                                if(DeleteTask.ERROR_CODE_DELETE_SUCCESS == result){
                                    mListFragment.updateCurrentList();
                                    Toast.makeText(mContext, R.string.delete_success, Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(mContext, R.string.delete_fail, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        break;
                    case R.id.rename:
                        rename(fileInfo);
                        break;
                    case R.id.favorite:
                        break;
                    case R.id.encryption:
                        break;
                    case R.id.share:
                        break;
                    case R.id.detail:
                        String fileName = fileInfo.getFileName();
                        String size = fileInfo.getFileSize();
                        if(fileInfo.isFolder()){
                            size = fileInfo.getFolderSizeHuman(fileInfo.getFile()) + "";
                        }
                        String absPath = fileInfo.getFileAbsolutePath();
                        String path = absPath.substring(0, absPath.length() - fileName.length());
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.AlertDialogCustom));
                        builder.setTitle(R.string.detail);
                        builder.setMessage(
                                mContext.getResources().getString(R.string.name) + " : " + fileName + "\n" +
                                mContext.getResources().getString(R.string.size) + " : " + size + "\n" +
                                mContext.getResources().getString(R.string.modify_date) + " : " + fileInfo.getFileLastModifiedDate() + "\n" +
                                mContext.getResources().getString(R.string.modify_time) + " : " + fileInfo.getFileLastModifiedTime() + "\n" +
                                mContext.getResources().getString(R.string.path) + " : " + path);
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                        break;
                }
                return true;
            }
        });
        mPopupMenu.show();
    }

    private void rename(final FileInfo info){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.new_folder_dialog, null);
        final EditText nameEdit = (EditText)view.findViewById(R.id.name);
        nameEdit.setHint(null);
        nameEdit.setText(info.getFileName());
        nameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("huangcx", "" + s.length());
                // TODO: 2018/3/16 长度为0时，确认按钮灰显
            }
        });
        if(!info.isFolder())
            nameEdit.setSelection(info.getFileName().lastIndexOf("."));
        else
            nameEdit.setSelection(info.getFileName().length());


        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.AlertDialogCustom));
        builder.setTitle(R.string.rename);
        builder.setView(view);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                File old = info.getFile();
                File newFile = new File(info.getParentFileAbsolutePath() + "/" + nameEdit.getText().toString());
                if(newFile.exists()){
                    Toast.makeText(mContext, R.string.file_exist, Toast.LENGTH_SHORT).show();
                }else{
                    old.renameTo(newFile);
                }
            }
        });

        AlertDialog dialog = builder.create();
        //以下两句用来打开AlertDialog时直接打开软键盘
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
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

        if(mCheckCount > 1){
            ((MainActivity) mListFragment.getActivity()).setRenameAndDetailMenuVisible(false);
        }else{
            ((MainActivity) mListFragment.getActivity()).setRenameAndDetailMenuVisible(true);
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
