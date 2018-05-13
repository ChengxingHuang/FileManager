package com.file.filemanager;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


public class MainPagerAdapter extends FragmentPagerAdapter {

    private static final int PAGE_FRAGMENT_COUNT = 2;

    private int[] mTabTitles = {R.string.category_storage, R.string.phone_storage};
    private MainActivity mMainActivity;

    private Fragment mCurrentFragment;
    private ListFragment mListFragment;
    private CategoryFragment mCategoryFragment;

    public MainPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mMainActivity = (MainActivity) context;
        mCategoryFragment = new CategoryFragment();
        mListFragment = new ListFragment();
    }

    @Override
    public Fragment getItem(int position) {
        if(0 == position) {
            return mCategoryFragment;
        }else{
            return mListFragment;
        }
    }

    @Override
    public int getCount() {
        return PAGE_FRAGMENT_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mMainActivity.getString(mTabTitles[position]);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object){
        super.setPrimaryItem(container, position, object);
        mCurrentFragment = (Fragment) object;
        if(mCurrentFragment == mListFragment){
            mMainActivity.setFloatActionButtonVisibility(View.VISIBLE);
        }else{
            mMainActivity.setFloatActionButtonVisibility(View.GONE);
        }
    }

    public boolean onBackPressed(){
        if(mCurrentFragment == mListFragment){
            return mListFragment.onBackPressed();
        }else if(mCurrentFragment == mCategoryFragment){
            return mCategoryFragment.onBackPressed();
        }else{
            return false;
        }
    }

    public void showRootPathList(){
        mListFragment.showRootPathList();
    }

    public void updateCurrentList(){
        if(mCurrentFragment == mListFragment){
            mListFragment.updateCurrentList();
        }else if(mCurrentFragment == mCategoryFragment){
            mCategoryFragment.updateCurrentList();
        }
    }

    public void clearFileList(){
        if(mCurrentFragment == mListFragment){
            mListFragment.clearFileList();
        }
    }

    public void updateFileList(final FileInfo fileInfo){
        if(mCurrentFragment == mListFragment){
            mListFragment.updateFileList(fileInfo);
        }
    }

    public void showSearchList(){
        if(mCurrentFragment == mListFragment){
            mListFragment.showSearchList();
        }
    }

    public void backToPreList(){
        if(mCurrentFragment == mListFragment){
            mListFragment.backToPreList();
        }else if(mCurrentFragment == mCategoryFragment){
            mCategoryFragment.backToPreList();
        }
    }

    public String getCurPath(){
        if(mCurrentFragment == mListFragment){
            return mListFragment.getCurPath();
        }

        return null;
    }

    public ArrayList<FileInfo> getCurCategoryList() {
        if (mCurrentFragment == mCategoryFragment) {
            return mCategoryFragment.getCurCategoryList();
        }

        return null;
    }
}
