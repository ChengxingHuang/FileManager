package com.file.filemanager;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    public static final int PAGE_FRAGMENT_COUNT = 2;

    private int[] mTabTitles = {R.string.category_storage, R.string.phone_storage};
    private MainActivity mMainActivity;

    private Fragment mCurrentFragment;
    private ListFragment mListFragment;
    private CategoryFragment mCategoryFragment;

    public MyFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mMainActivity = (MainActivity) context;
    }

    @Override
    public Fragment getItem(int position) {
        if(0 == position) {
            if(null == mCategoryFragment)
                mCategoryFragment = new CategoryFragment();
            return mCategoryFragment;
        }else{
            if(null == mListFragment)
                mListFragment = new ListFragment();
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

    public boolean onBackPressed(){
        if(mCurrentFragment == mListFragment){
            return mListFragment.onBackPressed();
        }else if(mCurrentFragment == mCategoryFragment){
            return mCategoryFragment.onBackPressed();
        }else{
            return false;
        }
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

    public void updateSearchList(ArrayList<FileInfo> list){
        if(mCurrentFragment == mListFragment){
            mListFragment.updateSearchList(list);
        }else if(mCurrentFragment == mCategoryFragment){
            mCategoryFragment.updateSearchList(list);
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
