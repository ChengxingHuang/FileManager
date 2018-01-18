package com.file.filemanager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.file.filemanager.Task.SearchTask;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private MyFragmentPagerAdapter mAdapter;
    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 0x01;
    public static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0x02;

    private ActionMode mActionMode;
    private TextView mItemCountView;
    private View mActionModeView;
    private Menu mClickMenu;
    private Menu mNormalMenu;
    private MainActivityCallBack mCallBack;

    private MenuItem mPasteMenuItem;
    private MenuItem mSearchMenuItem;
    private MenuItem mHomeMenuItem;
    private MenuItem mSortMenuItem;
    private SearchView mSearchView;

    private SearchTask mSearchTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MountStorageManager storageManager = MountStorageManager.getInstance();
        storageManager.init(this);

        FileInfo.init();

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

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        // TooBar 左边按钮拉出抽屉
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, 0, 0);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager_main);
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), this);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_normal_menu_option, menu);
        mNormalMenu = menu;
        mPasteMenuItem = mNormalMenu.findItem(R.id.action_paste);
        mSearchMenuItem = mNormalMenu.findItem(R.id.action_search);
        mHomeMenuItem = mNormalMenu.findItem(R.id.action_home);
        mSortMenuItem = mNormalMenu.findItem(R.id.action_sort);

        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchMenuItem);
        mSearchView.setQueryHint(getResources().getString(R.string.search_files));
        mSearchView.onActionViewCollapsed();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //要先暂停之前的搜索
                if((null != mSearchTask) && (mSearchTask.getStatus() == AsyncTask.Status.RUNNING)){
                    mSearchTask.cancel(true);
                }

                if(!"".equals(newText)){
                    // 按照路径或分类查找
                    String[] params = {mAdapter.getCurPath(), newText};
                    mSearchTask = new SearchTask(mAdapter.getCurCategoryList(), MainActivity.this);
                    mSearchTask.setOnSearchFinish(new SearchTask.OnSearchFinish() {
                        @Override
                        public void searchFinish(ArrayList<FileInfo> list) {
                            mAdapter.updateSearchList(list);
                        }
                    });
                    mSearchTask.execute(params);
                }else{
                    //搜索字串为空，返回搜索前的list
                    mAdapter.backToPreList();
                }

                if(!"".equals(newText)){
                    // 按照路径或分类查找
                    String[] params = {mAdapter.getCurPath(), newText};
                    mSearchTask = new SearchTask(mAdapter.getCurCategoryList(), MainActivity.this);
                    mSearchTask.setOnSearchFinish(new SearchTask.OnSearchFinish() {
                        @Override
                        public void searchFinish(ArrayList<FileInfo> list) {
                            mAdapter.updateSearchList(list);
                        }
                    });
                    mSearchTask.execute(params);
                }else{
                    //搜索字串为空，返回搜索前的list
                    mAdapter.backToPreList();
                }

                return true;
            }
        });
        //按了返回键，只是先让软键盘消失
//        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if(!hasFocus) {
//                    mSearchView.onActionViewCollapsed();
//                }
//            }
//        });
        //关闭SearchView监听
//        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
//            @Override
//            public boolean onClose() {
//                if(null == mAdapter.getCurPath() && null == mAdapter.getCurCategoryList())
//                    mAdapter.onBackPressed();
//                return false;
//            }
//        });

        mNormalMenu.findItem(R.id.sort_by).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showSortByDialog();
                return true;
            }
        });
        mNormalMenu.findItem(R.id.directory_sort_mode).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showDirectorySortModeDialog();
                return true;
            }
        });

        return true;
    }

    private void showSortByDialog(){
        int defaultSortBy = PreferenceUtils.getSortByValue(MainActivity.this);
        String[] sortByString = getResources().getStringArray(R.array.sort_by);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        AlertDialog dialog = builder.create();
        builder.setTitle(R.string.sort_by);
        builder.setSingleChoiceItems(sortByString, defaultSortBy, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PreferenceUtils.setSortByValue(MainActivity.this, which);
                mAdapter.updateCurrentList();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showDirectorySortModeDialog(){
        int defaultDirectorySortMode = PreferenceUtils.getDirectorSortModeValue(MainActivity.this);
        String[] directorySortModeString = getResources().getStringArray(R.array.directory_sort_mode);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        AlertDialog dialog = builder.create();
        builder.setTitle(R.string.directory_sort_mode);
        builder.setSingleChoiceItems(directorySortModeString, defaultDirectorySortMode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PreferenceUtils.setDirectorSortModeValue(MainActivity.this, which);
                mAdapter.updateCurrentList();
                dialog.dismiss();
            }
        });

        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
                || requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
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

    public void setRenameAndDetailMenuVisible(boolean visible){
        mClickMenu.findItem(R.id.detail).setVisible(visible);
        mClickMenu.findItem(R.id.rename).setVisible(visible);
    }

    public void setItemCountView(String count){
        if(null != mItemCountView){
            mItemCountView.setText(count);
        }
    }

    public void setCallBack(MainActivityCallBack callBack){
        mCallBack = callBack;
    }

    public interface MainActivityCallBack{
        void cleanCheck();
    }

    public ActionMode.Callback mActionModeCallback = new ActionMode.Callback(){

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            mActionModeView = getLayoutInflater().inflate(R.layout.action_mode, null);
            mode.setCustomView(mActionModeView);

            mItemCountView = (TextView) mActionModeView.findViewById(R.id.item_count);

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
}
