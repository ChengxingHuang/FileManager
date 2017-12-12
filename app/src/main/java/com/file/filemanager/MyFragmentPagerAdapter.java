package com.file.filemanager;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;


public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    public static final int PAGE_FRAGMENT_COUNT = 2;

    private int[] mTabTitles = {R.string.category_storage, R.string.phone_storage};
    private Context mContext;

    private Fragment mCurrentFragment;
    private ListFragment mListFragment;
    private CategoryFragment mCategoryFragment;

    public MyFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if(0 == position) {
            mCategoryFragment = new CategoryFragment();
            return mCategoryFragment;
        }else{
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
        return mContext.getResources().getString(mTabTitles[position]);
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
    }

    public void updateCurrentList(){
        if(null != mListFragment) {
            mListFragment.updateCurrentList();
        }
    }
}
