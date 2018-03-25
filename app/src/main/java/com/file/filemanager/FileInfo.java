package com.file.filemanager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by huang on 2017/8/4.
 */

public class FileInfo {
    public static final String TAG = "FileInfo";

    private static final String UNIT_B = "B";
    private static final String UNIT_KB = "KB";
    private static final String UNIT_MB = "MB";
    private static final String UNIT_GB = "GB";
    private static final String UNIT_TB = "TB";

    private static final int UNIT_INTERVAL = 1024;
    private static final double ROUNDING_OFF = 0.005;
    private static final int DECIMAL_NUMBER = 100;

    private Context mContext;
    private File mFile;
    private String mFileAbsolutePath;
    private String mParentFileAbsolutePath;
    private Drawable mFileTypeImage;
    private String mFileName;
    private String mFileSize;
    private boolean mIsFolder;
    private String mFileLastModifiedDate;
    private String mFileLastModifiedTime;
    private boolean mIsChecked;

    public FileInfo(Context context, String path){
        if(path != null){
            mContext = context;
            mFile = new File(path);
            mFileAbsolutePath = path;
            mFileName = mFile.getName();
            mIsFolder = mFile.isDirectory();
            if(mIsFolder) {
                mFileSize = getChildFilesCount(context) + "";
                mFileTypeImage = mContext.getResources().getDrawable(R.drawable.file_type_folder, null);
            }else{
                mFileSize = sizeToHumanString(mFile.length());
                String mimeType = getMimeType();

                if(Utils.getRegisterMimeTypeMap().containsKey(mimeType)){
                    mFileTypeImage = mContext.getResources().getDrawable(Utils.getRegisterMimeTypeMap().get(mimeType), null);
                }else if("application/vnd.android.package-archive".equals(mimeType)){
                    // 解析APK，获得APK图标
                    mFileTypeImage = getAPKIcon(mFileAbsolutePath);
                }else {
                    mFileTypeImage = mContext.getResources().getDrawable(R.drawable.file_type_unknow, null);
                }
            }

            mParentFileAbsolutePath = getParentPath(path);
            mFileLastModifiedDate = getLastModifiedDate(mFile);
            mFileLastModifiedTime = getLastModifiedTime(mFile);
            mIsChecked = false;
        }
    }

    private Drawable getAPKIcon(String apkPath){
        PackageManager pm = mContext.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);

        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "OutOfMemory when getAPKIcon:" + e.toString());
            }
        }

        return null;
    }


    // 是否为隐藏文件
    public boolean isHideFileType(){
        return mFileName.startsWith(".");
    }

    public Drawable getFileTypeImage() {
        return mFileTypeImage;
    }

    public File getFile(){
        return mFile;
    }

    public String getFileAbsolutePath() {
        return mFileAbsolutePath;
    }

    public String getParentFileAbsolutePath() {
        return mParentFileAbsolutePath;
    }

    public String getFileName() {
        return mFileName;
    }

    public String getSuffix(){
        return mFileName.substring(mFileName.lastIndexOf("."), mFileName.length());
    }

    public String getFileSize() {
        return mFileSize;
    }

    public long getFolderSize(File file){
        if (file.exists()) {
            //如果是目录则递归计算其内容的总大小
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                long size = 0;
                for (File f : children) {
                    if(f.getName().startsWith(".") && !PreferenceUtils.getShowHideFileValue(mContext))
                        break;
                    size += getFolderSize(f);
                }
                return size;
            } else {
                if(file.getName().startsWith(".") && !PreferenceUtils.getShowHideFileValue(mContext)){
                    return 0;
                }else {
                    return file.length();
                }
            }
        }else{
            return -1;
        }
    }

    public String getFolderSizeHuman(File file){
        return sizeToHumanString(getFolderSize(file));
    }

    public boolean isFolder() {
        return mIsFolder;
    }

    public String getFileLastModifiedDate() {
        return mFileLastModifiedDate;
    }

    public String getFileLastModifiedTime() {
        return mFileLastModifiedTime;
    }

    public void setChecked(boolean isChecked){
        mIsChecked = isChecked;
    }

    public boolean isChecked(){
        return mIsChecked;
    }

    /**
     * 获取文件夹中包含多少子文件
     */
    public int getChildFilesCount(Context context){
        boolean canShowHideFile = PreferenceUtils.getShowHideFileValue(context);

        if(canShowHideFile){
            return mFile.list().length;
        }else {
            int length = 0;
            for (String file : mFile.list()) {
                if (file.startsWith(".")) {
                    continue;
                }
                length++;
            }
            return length;
        }
    }

    /**
     * 将byte转换为B/KB/MB/GB/TB
     */
    private String sizeToHumanString(long size) {
        String unit = UNIT_B;
        if (size < DECIMAL_NUMBER) {
            return Long.toString(size) + " " + unit;
        }

        unit = UNIT_KB;
        double sizeDouble = (double) size / (double) UNIT_INTERVAL;
        if (sizeDouble > UNIT_INTERVAL) {
            sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
            unit = UNIT_MB;
        }
        if (sizeDouble > UNIT_INTERVAL) {
            sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
            unit = UNIT_GB;
        }
        if (sizeDouble > UNIT_INTERVAL) {
            sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
            unit = UNIT_TB;
        }

        long sizeInt = (long) ((sizeDouble + ROUNDING_OFF) * DECIMAL_NUMBER);
        double formatedSize = ((double) sizeInt) / DECIMAL_NUMBER;

        if (formatedSize == 0) {
            return "0" + " " + unit;
        } else {
            return Double.toString(formatedSize) + " " + unit;
        }
    }

    /**
     * 获取指定路径的父目录
     */
    private String getParentPath(String filePath) {
        int sepIndex = filePath.lastIndexOf("/");
        if (sepIndex >= 0) {
            return filePath.substring(0, sepIndex);
        }
        return "";
    }

    /**
     * 获取文件/文件夹的最后修改日期
     */
    private String getLastModifiedDate(File file){
        long lastChangeTime = file.lastModified();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return dateFormat.format(lastChangeTime);
    }

    /**
     * 获取文件/文件夹的最后修改时间
     */
    private String getLastModifiedTime(File file){
        long lastChangeTime = file.lastModified();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        return dateFormat.format(lastChangeTime);
    }

    /*
     * 根据文件名（包含路径）获取文件的MIME类型
     */
    public String getMimeType(){
        if((null == mFileAbsolutePath) || (mFileAbsolutePath.endsWith(".")) || (!mFileAbsolutePath.contains("."))){
            return null;
        }

        int index = mFileAbsolutePath.lastIndexOf(".");
        if (index != -1) {
            String extension = mFileAbsolutePath.substring(index + 1).toLowerCase(Locale.US);
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        return null;
    }

    static class NameComparator implements Comparator{
        private int mSortBy = 0;
        private int mDirectorSortMode = 0;
        private boolean mIsAsc = true;

        public NameComparator(Context context){
            mSortBy = PreferenceUtils.getSortByValue(context);
            mDirectorSortMode = PreferenceUtils.getDirectorSortModeValue(context);
            mIsAsc = PreferenceUtils.getOrderTypeValue(context);
        }

        private int sort(Object o1, Object o2){
            if (mSortBy == PreferenceUtils.SORT_BY_NAME) {
                String s1 = ((FileInfo) o1).getFileName().toUpperCase();
                String s2 = ((FileInfo) o2).getFileName().toUpperCase();

                if (mIsAsc == PreferenceUtils.ORDER_TYPE_ASCEND) {
                    return s1.compareTo(s2);
                } else {
                    return s2.compareTo(s1);
                }
            }else if(mSortBy == PreferenceUtils.SORT_BY_TYPE){
                // FIXME: 2017/12/4 类型只针对文件，通过后缀判断，可能需要修改
                if(!((FileInfo) o1).isFolder() && !((FileInfo) o2).isFolder()) {
                    String[] tmp1 = ((FileInfo) o1).getFileName().split("\\.");
                    String[] tmp2 = ((FileInfo) o2).getFileName().split("\\.");
                    String suffix1 = tmp1[tmp1.length - 1];
                    String suffix2 = tmp2[tmp2.length - 1];

                    return suffix1.compareTo(suffix2);
                }else {
                    String s1 = ((FileInfo) o1).getFileName().toUpperCase();
                    String s2 = ((FileInfo) o2).getFileName().toUpperCase();

                    if (mIsAsc == PreferenceUtils.ORDER_TYPE_ASCEND) {
                        return s1.compareTo(s2);
                    } else {
                        return s2.compareTo(s1);
                    }
                }
            }else if(mSortBy == PreferenceUtils.SORT_BY_SIZE){
                long s1 = ((FileInfo) o1).getFolderSize(((FileInfo) o1).getFile());
                long s2 = ((FileInfo) o2).getFolderSize(((FileInfo) o2).getFile());

                if (mIsAsc == PreferenceUtils.ORDER_TYPE_ASCEND) {
                    return s1 > s2 ? 1 : -1;
                } else {
                    return s1 < s2 ? 1 : -1;
                }
            }else if(mSortBy == PreferenceUtils.SORT_BY_LAST_MODIFY){
                long s1 = ((FileInfo) o1).getFile().lastModified();
                long s2 = ((FileInfo) o2).getFile().lastModified();

                if (mIsAsc == PreferenceUtils.ORDER_TYPE_ASCEND) {
                    return s1 > s2 ? 1 : -1;
                } else {
                    return s1 < s2 ? 1 : -1;
                }
            }

            return 1;
        }

        @Override
        public int compare(Object o1, Object o2) {
            boolean b1 = ((FileInfo)o1).isFolder();
            boolean b2 = ((FileInfo)o2).isFolder();

            if (b1 == b2) {
                return sort(o1, o2);
            } else {
                if(((mDirectorSortMode == PreferenceUtils.DIRECTOR_SORT_MODE_FOLDER_ON_TOP) && b1)
                        || (mDirectorSortMode == PreferenceUtils.DIRECTOR_SORT_MODE_FILE_ON_TOP) && b2) {
                    return -1;
                }else if(mDirectorSortMode == PreferenceUtils.DIRECTOR_SORT_MODE_NONE_ON_TOP){
                    return sort(o1, o2);
                }else{
                    return 1;
                }
            }
        }
    }
}
