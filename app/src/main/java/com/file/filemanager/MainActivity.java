package com.file.filemanager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.file.filemanager.Service.FileManagerService;
import com.file.filemanager.Service.FileOperator;
import com.file.filemanager.Service.FileOperatorListener;
import com.file.filemanager.Task.TaskInfo;
import com.file.filemanager.Utils.MediaStoreUtils;
import com.file.filemanager.Utils.OtherUtils;
import com.file.filemanager.Utils.PreferenceUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.file.filemanager.Task.BaseAsyncTask.ERROR_CODE_CURSOR_NULL;
import static com.file.filemanager.Task.BaseAsyncTask.ERROR_CODE_DELETE_FAIL;
import static com.file.filemanager.Task.BaseAsyncTask.ERROR_CODE_DELETE_NO_PERMISSION;
import static com.file.filemanager.Task.BaseAsyncTask.ERROR_CODE_FILE_EXIST;
import static com.file.filemanager.Task.BaseAsyncTask.ERROR_CODE_FILE_NOT_EXIST;
import static com.file.filemanager.Task.BaseAsyncTask.ERROR_CODE_MKDIR_ERROR;
import static com.file.filemanager.Task.BaseAsyncTask.ERROR_CODE_NO_MATCH_FILES;
import static com.file.filemanager.Task.BaseAsyncTask.ERROR_CODE_SUCCESS;
import static com.file.filemanager.Task.BaseAsyncTask.ERROR_CODE_USER_CANCEL;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 0x01;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0x02;

    private static final String TAG = "MainActivity";

    private DrawerLayout mDrawerLayout;
    private ActionMode mActionMode;
    private TextView mItemCountView;
    private Menu mClickMenu;
    private FloatingActionButton mFloatingActionButton;
    private MenuItem mPasteMenuItem;
    private SearchView mSearchView;
    private AlertDialog.Builder mDialogBuilder;
    private ServiceConnection mServiceConnection;

    private MainPagerAdapter mAdapter;
    private MainActivityCallBack mCallBack;
    private Context mContext;
    private FileOperator mFileOperator;

    private List<String> mCopySrcList;
    private long mTotalOperatorSize;
    private boolean mIsCut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        MountStorageManager storageManager = MountStorageManager.getInstance();
        storageManager.init(this);
        OtherUtils.mimeTypeInit();

        mServiceConnection = new FileServiceConnection();
        Intent intent = new Intent(this, FileManagerService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        requestPermission();

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, 0, 0);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager_main);
        mAdapter = new MainPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(mAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_main);
        tabLayout.setupWithViewPager(viewPager);

        NavigationView navigationView = (NavigationView)findViewById(R.id.navigation_main);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                switch (itemId){
                    case R.id.menu_setting: {
                        Intent i = new Intent(MainActivity.this, SettingActivity.class);
                        startActivity(i);
                        break;
                    }
                    case R.id.menu_about: {
                        Intent i = new Intent(MainActivity.this, AboutActivity.class);
                        startActivity(i);
                        break;
                    }
                }
                //关闭NavigationView
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        mFloatingActionButton = (FloatingActionButton)findViewById(R.id.floatingActionButton);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                View view = inflater.inflate(R.layout.new_folder_dialog, null);
                final EditText nameEdit = (EditText)view.findViewById(R.id.name);

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.AlertDialogCustom));
                builder.setTitle(R.string.create_folder);
                builder.setView(view);
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        String path = mAdapter.getCurPath() + File.separator + nameEdit.getEditableText().toString();
                        mFileOperator.createFolder(path, new CreateFolderOperationListener());
                    }
                });

                AlertDialog dialog = builder.create();
                //以下两句用来打开AlertDialog时直接打开软键盘
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_normal_menu_option, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        mPasteMenuItem = menu.findItem(R.id.action_paste);

        mSearchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        mSearchView.setQueryHint(getString(R.string.search_files));
        mSearchView.onActionViewCollapsed();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!"".equals(newText)){
                    // 按照路径或分类查找
                    mFileOperator.searchFile(newText, mAdapter.getCurPath(), getContentResolver(), new SearchOperationListener());
                }else{
                    //搜索字串为空，返回搜索前的list
                    mAdapter.backToPreList();
                }

                return true;
            }
        });

        menu.findItem(R.id.sort_by).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showSortByDialog();
                return true;
            }
        });
        menu.findItem(R.id.directory_sort_mode).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showDirectorySortModeDialog();
                return true;
            }
        });

        //默认情况不显示黏贴按键
        mPasteMenuItem.setVisible(false);
        mPasteMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mPasteMenuItem.setVisible(false);
                mFileOperator.pasteFile(mCopySrcList, mAdapter.getCurPath(), new PasteOperationListener());
                return true;
            }
        });

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
                || requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mContext, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        boolean needBack = true;

        //返回键关闭SearchView
        if (!mSearchView.isIconified()) {
            mSearchView.onActionViewCollapsed();
            needBack = false;
        }

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        } else if(mAdapter.onBackPressed()){
            return;
        }

        if(needBack) {
            super.onBackPressed();
        }
    }

    public void startActionMode(){
        mActionMode = startSupportActionMode(mActionModeCallback);
    }

    public void finishActionMode(){
        mActionMode.finish();
    }

    public void setPasteIconVisible(boolean visible){
        mPasteMenuItem.setVisible(visible);
    }

    public void setSrcCopyPath(List<FileInfo> fileInfoList, boolean isCut){
        mIsCut = isCut;
        mCopySrcList = new ArrayList<>();
        for(FileInfo fileInfo : fileInfoList){
            mCopySrcList.add(fileInfo.getFileAbsolutePath());
            mTotalOperatorSize += fileInfo.getMachineSize();
        }
    }

    public void setRenameAndDetailMenuVisible(boolean visible){
        mClickMenu.findItem(R.id.detail).setVisible(visible);
        mClickMenu.findItem(R.id.rename).setVisible(visible);
    }

    public void setItemCountView(String count){
        if(null != mItemCountView){
            mItemCountView.setText(count);
        }
    }

    public void setFloatActionButtonVisibility(int visibility){
        mFloatingActionButton.setVisibility(visibility);
    }

    public void setCallBack(MainActivityCallBack callBack){
        mCallBack = callBack;
    }

    public void deleteFiles(List<String> paths){
        mFileOperator.deleteFile(paths, new DeleteOperationListener());
    }

    public void showFiles(String path, FileOperatorListener listener){
        mFileOperator.showFile(path, listener);
    }

    public void sortFiles(FileOperatorListener listener){
        mFileOperator.sortFile(listener);
    }

    public interface MainActivityCallBack{
        void cleanCheck();
    }

    public ActionMode.Callback mActionModeCallback = new ActionMode.Callback(){

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            View actionModeView = getLayoutInflater().inflate(R.layout.action_mode, null);
            mode.setCustomView(actionModeView);

            mItemCountView = (TextView) actionModeView.findViewById(R.id.item_count);

            inflater.inflate(R.menu.toolbar_click_menu_option, menu);
            mClickMenu = menu;

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mCallBack.cleanCheck();
        }
    };

    private void requestPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void showSortByDialog(){
        int defaultSortBy = PreferenceUtils.getSortByValue(mContext);
        String[] sortByString = getResources().getStringArray(R.array.sort_by);

        mDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.AlertDialogCustom));
        mDialogBuilder.setTitle(R.string.sort_by);
        mDialogBuilder.setSingleChoiceItems(sortByString, defaultSortBy, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PreferenceUtils.setSortByValue(mContext, which);
                mAdapter.updateCurrentList();
                dialog.dismiss();
            }
        });
        mDialogBuilder.show();
    }

    private void showDirectorySortModeDialog(){
        int defaultDirectorySortMode = PreferenceUtils.getDirectorSortModeValue(mContext);
        String[] directorySortModeString = getResources().getStringArray(R.array.directory_sort_mode);

        mDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.AlertDialogCustom));
        mDialogBuilder.setTitle(R.string.directory_sort_mode);
        mDialogBuilder.setSingleChoiceItems(directorySortModeString, defaultDirectorySortMode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PreferenceUtils.setDirectorSortModeValue(mContext, which);
                mAdapter.updateCurrentList();
                dialog.dismiss();
            }
        });

        mDialogBuilder.show();
    }

    private void showErrorAlert(String type, String path){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.AlertDialogCustom));
        AlertDialog dialog = builder.create();
        dialog.setTitle(android.R.string.dialog_alert_title);
        dialog.setMessage(type + ":" + path);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    class FileServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mFileOperator = (FileOperator)service;
            mAdapter.showMainUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    //删除操作
    class DeleteOperationListener implements FileOperatorListener{

        @Override
        public void onTaskPrepare() {

        }

        @Override
        public void onTaskProgress(TaskInfo progressInfo) {

        }

        @Override
        public void onTaskResult(TaskInfo taskInfo) {
            switch(taskInfo.mErrorCode){
                case ERROR_CODE_DELETE_NO_PERMISSION:
                    Log.d(TAG, "No delete permission:" + taskInfo.mErrorPath);
                    showErrorAlert(getString(R.string.delete_no_permission), taskInfo.mErrorPath);
                    break;

                case ERROR_CODE_DELETE_FAIL:
                    Log.d(TAG, "Delete fail:" + taskInfo.mErrorPath);
                    showErrorAlert(getString(R.string.delete_fail), taskInfo.mErrorPath);
                    break;

                case ERROR_CODE_FILE_NOT_EXIST:
                    showErrorAlert(getString(R.string.file_not_exist), taskInfo.mErrorPath);
                    break;

                case ERROR_CODE_SUCCESS:
                    MediaStoreUtils.deleteFileInMediaStore(mContext, taskInfo.mErrorPath);
                    mAdapter.updateCurrentList();
                    break;
            }
        }
    }

    //黏贴操作
    class PasteOperationListener implements FileOperatorListener{

        private List<String> mNewList;
        private ProgressDialog mCopyProcessDialog;

        @Override
        public void onTaskPrepare() {
            mCopyProcessDialog = new ProgressDialog(new ContextThemeWrapper(mContext, R.style.AlertDialogCustom));
            mCopyProcessDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mCopyProcessDialog.setTitle(R.string.copying);
            mCopyProcessDialog.setMax(100);
            mCopyProcessDialog.setMessage("");
            mCopyProcessDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mFileOperator.cancelTask(FileOperator.TASK_PASTE_ID);
                            mCopyProcessDialog.dismiss();
                        }
                    });
            mCopyProcessDialog.show();
            mNewList = new ArrayList<>();
        }

        @Override
        public void onTaskProgress(TaskInfo progressInfo) {
            long readSize = progressInfo.getProgress();
            float percent = ((float)readSize / (float)mTotalOperatorSize) * 100;
            mCopyProcessDialog.setMessage(progressInfo.getCurName());
            mCopyProcessDialog.setProgress(Math.round(percent));
            if(!mNewList.contains(progressInfo.mErrorPath)) {
                mNewList.add(progressInfo.mErrorPath);
            }
        }

        @Override
        public void onTaskResult(TaskInfo taskInfo) {
            mCopyProcessDialog.dismiss();
            switch(taskInfo.mErrorCode){
                //黏贴成功，如果是剪切，需要删除源文件
                case ERROR_CODE_SUCCESS:
                    if(mIsCut){
                        mFileOperator.deleteFile(mCopySrcList, new DeleteOperationListener());
                    }
                    mAdapter.updateCurrentList();
                    MediaStoreUtils.scanPathForMediaStore(mContext, mNewList);
                    break;

                //用户取消黏贴，删除正在黏贴的部分
                case ERROR_CODE_USER_CANCEL:
                    List<String> list = new ArrayList<>();
                    list.add(taskInfo.mErrorPath);
                    mFileOperator.deleteFile(list, new DeleteOperationListener());
                    mAdapter.updateCurrentList();
                    break;

                //创建文件夹失败
                case ERROR_CODE_MKDIR_ERROR:
                    Log.d(TAG, "mkdir fail when copy file:" + taskInfo.mErrorPath);
                    showErrorAlert(getString(R.string.create_folder_error), taskInfo.mErrorPath);
                    break;

                //源文件不存在
                case ERROR_CODE_FILE_NOT_EXIST:
                    Log.d(TAG, "Copy source file not exist:" + taskInfo.mErrorPath);
                    showErrorAlert(getString(R.string.file_not_exist), taskInfo.mErrorPath);
                    break;
            }
        }
    }

    //新建文件夹操作
    class CreateFolderOperationListener implements FileOperatorListener{

        @Override
        public void onTaskPrepare() {

        }

        @Override
        public void onTaskProgress(TaskInfo progressInfo) {

        }

        @Override
        public void onTaskResult(TaskInfo taskInfo) {
            switch (taskInfo.mErrorCode){
                case ERROR_CODE_FILE_EXIST:
                    Toast.makeText(MainActivity.this, R.string.folder_exist, Toast.LENGTH_SHORT).show();
                    break;

                case ERROR_CODE_SUCCESS:
                    MediaStoreUtils.scanPathForMediaStore(mContext, taskInfo.mErrorPath);
                    mAdapter.updateCurrentList();
                    break;

                case ERROR_CODE_MKDIR_ERROR:
                    Toast.makeText(MainActivity.this, R.string.create_folder_error, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    //搜索操作
    class SearchOperationListener implements FileOperatorListener{

        @Override
        public void onTaskPrepare() {
            mAdapter.clearFileList();
        }

        @Override
        public void onTaskProgress(TaskInfo taskInfo) {
            mAdapter.updateFileList(taskInfo.getFileInfo());
        }

        @Override
        public void onTaskResult(TaskInfo taskInfo) {
            switch (taskInfo.mErrorCode){
                case ERROR_CODE_SUCCESS:
                    mAdapter.showSearchList();
                    break;

                case ERROR_CODE_CURSOR_NULL:
                    break;

                case ERROR_CODE_NO_MATCH_FILES:
                    Toast.makeText(mContext, R.string.no_match_file, Toast.LENGTH_SHORT).show();
                    break;

                case ERROR_CODE_USER_CANCEL:
                    break;
            }
        }
    }
}
