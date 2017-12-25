package com.file.filemanager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private MyFragmentPagerAdapter mAdapter;
    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 0x01;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MountStorageManager storageManager = MountStorageManager.getInstance();
        storageManager.init(this);

        FileInfo.init();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }else{
            //
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
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO: 2017/12/3 查找 
                Log.d("huangcx", "input text = " + newText);
                if(PreferenceUtils.getSearchRootValue(MainActivity.this)){
                    // TODO: 2017/12/24 查找手机本身和SD卡 
                }else {
                    // 按照路径查找或者按照分类查找
                    String[] params = {mAdapter.getCurPath(), newText};
                    SearchTask task = new SearchTask(mAdapter.getCurList(), MainActivity.this);
                    task.execute(params);
                    task.setOnSearchFinish(new SearchTask.OnSearchFinish() {
                        @Override
                        public void searchFinish(ArrayList<FileInfo> list) {
                            mAdapter.updateSearchList(list);
                        }
                    });
                }
                return true;
            }
        });
        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    mSearchView.onActionViewCollapsed();
                }
            }
        });

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
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // callPhone();
            } else {
                // Permission Denied
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        } else if(mAdapter.onBackPressed()){
            return;
        }

        super.onBackPressed();
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
