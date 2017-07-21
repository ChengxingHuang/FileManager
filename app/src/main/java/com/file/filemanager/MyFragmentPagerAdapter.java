package com.file.filemanager;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    public static final int PAGE_FRAGMENT_COUNT = 2;

    private int[] mTabTitles = {R.string.category_storage, R.string.phone_storage};
    private Context mContext;

    public MyFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if(0 == position) {
            return new CategoryFragment();
        }else{
            return new ListFragment();
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
}
