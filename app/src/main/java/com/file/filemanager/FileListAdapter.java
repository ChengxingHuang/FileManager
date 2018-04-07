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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import com.file.filemanager.Task.DeleteTask;

import java.io.File;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.MyViewHolder> implements MainActivity.MainActivityCallBack{
    private static final String TAG = "FileListAdapter";

    private List<FileInfo> mDataList;
    private LayoutInflater mInflater;
    private ListFragment mListFragment;
    private MainActivity mMainActivity;

    private int mCheckCount = 0;
    private boolean[] mIsPositionChecked;
    private boolean mIsCurPathInFavorite;

    //文件或者文件夹名称不能包含以下特殊字符
    private static String[] SPECIAL = {"/", "?", "*"};

    public FileListAdapter(Context context, List<FileInfo> dataList, ListFragment fragment){
        mDataList = dataList;
        mInflater = LayoutInflater.from(context);
        mListFragment = fragment;
        mIsPositionChecked = null;
        mMainActivity = (MainActivity) context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mInflater.inflate(R.layout.file_list_item, parent, false));
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
                        String mimeType = fileInfo.getMimeType();
                        Log.d(TAG, "mimeType = " + mimeType);
                        if (null != mimeType) {
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(fileInfo.getFile()), mimeType);
                            mMainActivity.startActivity(intent);
                        } else {
                            Toast.makeText(mMainActivity, mMainActivity.getString(R.string.unknown_type), Toast.LENGTH_SHORT).show();
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
                    mMainActivity.setCallBack(FileListAdapter.this);
                }

                setCheckStatus(fileInfo, position);
                notifyItemChanged(position);

                return true;
            }
        });
    }

    private void showPopupMenu(View anchor, final FileInfo fileInfo){
        PopupMenu popupMenu = new PopupMenu(mMainActivity, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_window, popupMenu.getMenu());
        if(mIsCurPathInFavorite = mMainActivity.isPathInFavorite(fileInfo.getFileAbsolutePath())) {
            MenuItem favorite = popupMenu.getMenu().findItem(R.id.favorite);
            favorite.setTitle(R.string.cancel_favorite);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.copy:
                        copy(false, fileInfo);
                        break;
                    case R.id.cut:
                        copy(true, fileInfo);
                        break;
                    case R.id.delete:
                        delete(fileInfo);
                        break;
                    case R.id.rename:
                        rename(fileInfo);
                        break;
                    case R.id.favorite:
                        favorite(fileInfo);
                        break;
                    case R.id.share:
                        share(fileInfo);
                        break;
                    case R.id.detail:
                        showDetails(fileInfo);
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void copy(boolean isCut, final FileInfo fileInfo){
        mMainActivity.setPasteIconVisible(true);
        mMainActivity.setCutMode(isCut);
        mMainActivity.setSrcPastePath(fileInfo.getFileAbsolutePath());
    }

    private void delete(final FileInfo fileInfo){
//        DeleteTask task = new DeleteTask();
//        task.execute(fileInfo.getFileAbsolutePath());
//        task.setHandleDeleteMsg(new DeleteTask.HandlerDeleteMessage() {
//            @Override
//            public void deleteFinish(int result) {
//                if(DeleteTask.ERROR_CODE_DELETE_SUCCESS == result){
//                    mListFragment.updateCurrentList();
//                    Toast.makeText(mMainActivity, R.string.delete_success, Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(mMainActivity, R.string.delete_fail, Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    private void rename(final FileInfo fileInfo){
        LayoutInflater inflater = LayoutInflater.from(mMainActivity);
        View view = inflater.inflate(R.layout.new_folder_dialog, null);
        final EditText nameEdit = (EditText)view.findViewById(R.id.name);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mMainActivity, R.style.AlertDialogCustom));
        AlertDialog dialog = builder.create();
        dialog.setTitle(R.string.rename);
        dialog.setView(view);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, mMainActivity.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File old = fileInfo.getFile();
                File newFile = new File(fileInfo.getParentFileAbsolutePath() + "/" + nameEdit.getText().toString());
                if(newFile.exists()){
                    Toast.makeText(mMainActivity, R.string.file_exist, Toast.LENGTH_SHORT).show();
                }else{
                    if(!old.renameTo(newFile))
                        Toast.makeText(mMainActivity, R.string.rename_fail, Toast.LENGTH_SHORT).show();
                    else
                        mListFragment.updateCurrentList();
                }
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, mMainActivity.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show(); //这个不能放在监听文字改变事件之后，否则btn会是null???

        final Button btn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        nameEdit.setHint(null);
        nameEdit.setText(fileInfo.getFileName());
        nameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean includeSpecial = false;
                //包含特殊字符
                for (String str : SPECIAL) {
                    if(s.toString().contains(str)) {
                        Toast.makeText(mMainActivity, R.string.special_tips, Toast.LENGTH_SHORT).show();
                        includeSpecial = true;
                        break;
                    }
                }

                if(0 == s.length() || includeSpecial)
                    btn.setEnabled(false);
                else
                    btn.setEnabled(true);
            }
        });
        if(!fileInfo.isFolder())
            nameEdit.setSelection(fileInfo.getFileName().lastIndexOf("."));
        else
            nameEdit.setSelection(fileInfo.getFileName().length());
    }

    private void favorite(final FileInfo fileInfo){
        if(mIsCurPathInFavorite){
            mMainActivity.deleteFromFavorite(fileInfo.getFileAbsolutePath());
        }else{
            mMainActivity.addToFavorite(fileInfo.getFileAbsolutePath());
        }
    }

    private void share(final FileInfo fileInfo){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType(fileInfo.getMimeType());
        Uri uri = Uri.fromFile(fileInfo.getFile());
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        mMainActivity.startActivity(Intent.createChooser(intent, mMainActivity.getString(R.string.share)));
    }

    private void showDetails(final FileInfo fileInfo){
        String fileName = fileInfo.getFileName();
        String size = fileInfo.getFileSize();
        if(fileInfo.isFolder()){
            size = fileInfo.getFolderSizeHuman(fileInfo.getFile()) + "";
        }
        String path = fileInfo.getParentFileAbsolutePath();
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mMainActivity, R.style.AlertDialogCustom));
        AlertDialog dialog = builder.create();
        dialog.setTitle(R.string.detail);
        dialog.setMessage(
                mMainActivity.getString(R.string.name) + " : " + fileName + "\n" +
                        mMainActivity.getString(R.string.size) + " : " + size + "\n" +
                        mMainActivity.getString(R.string.modify_date) + " : " + fileInfo.getFileLastModifiedDate() + "\n" +
                        mMainActivity.getString(R.string.modify_time) + " : " + fileInfo.getFileLastModifiedTime() + "\n" +
                        mMainActivity.getString(R.string.path) + " : " + path);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, mMainActivity.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void setCheckStatus(FileInfo fileInfo, int position){
        if(mIsPositionChecked[position]){
            //取消选中
            mIsPositionChecked[position] = false;
            fileInfo.setChecked(false);

            mCheckCount--;
            if(0 == mCheckCount){
                mMainActivity.finishActionMode();
                mIsPositionChecked = null;
            }else {
                mMainActivity.setItemCountView(mCheckCount + "");
            }
        }else{
            //选中
            mIsPositionChecked[position] = true;
            fileInfo.setChecked(true);

            mCheckCount++;
            if(1 == mCheckCount) {
                mMainActivity.startActionMode();
                mListFragment.setFloatActionButtonVisibility(View.INVISIBLE);
            }
            mMainActivity.setItemCountView(mCheckCount + "");
        }

        if(mCheckCount > 1){
            mMainActivity.setRenameAndDetailMenuVisible(false);
        }else{
            mMainActivity.setRenameAndDetailMenuVisible(true);
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

    static class MyViewHolder extends RecyclerView.ViewHolder {
        View mWholeView;
        ImageView mFileTypeImage;
        TextView mFileNameText;
        TextView mFileSizeText;
        TextView mLastModifiedDateText;
        TextView mLastModifiedTimeText;
        ImageView mOptionMenuImage;

        MyViewHolder(View view) {
            super(view);
            mWholeView = view.findViewById(R.id.file_list_item_view);
            mFileTypeImage = (ImageView) view.findViewById(R.id.file_type_image);
            mFileNameText = (TextView) view.findViewById(R.id.file_name_text);
            mLastModifiedDateText = (TextView) view.findViewById(R.id.last_change_date_text);
            mLastModifiedTimeText = (TextView) view.findViewById(R.id.last_change_time_text);
            mFileSizeText = (TextView) view.findViewById(R.id.include_file_count_text);
            mOptionMenuImage = (ImageView) view.findViewById(R.id.option_menu_image);
        }
    }
}
