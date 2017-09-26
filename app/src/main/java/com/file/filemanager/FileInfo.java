package com.file.filemanager;

import android.content.Context;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by huang on 2017/8/4.
 */

public class FileInfo {
    public static final String UNIT_B = "B";
    public static final String UNIT_KB = "KB";
    public static final String UNIT_MB = "MB";
    public static final String UNIT_GB = "GB";
    public static final String UNIT_TB = "TB";

    private static final int UNIT_INTERVAL = 1024;
    private static final double ROUNDING_OFF = 0.005;
    private static final int DECIMAL_NUMBER = 100;

    private File mFile;
    private String mFileAbsolutePath;
    private String mParentFileAbsolutePath;
    private int mFileTypeImage;
    private String mFileName;
    private String mFileSize;
    private boolean mIsFolder;
    private String mFileLastModifiedDate;
    private String mFileLastModifiedTime;

    public FileInfo(Context context, String path){
        if(path != null){
            mFile = new File(path);
            mFileAbsolutePath = path;
            mFileName = mFile.getName();
            mIsFolder = mFile.isDirectory();
            if(mIsFolder) {
                mFileSize = getChildFilesCount(context) + "";
                mFileTypeImage = R.drawable.file_type_folder;
            }else{
                mFileSize = sizeToHumanString(mFile.length());
                // TODO: 2017/9/25 根据不同的类型显示不同的图标 
                mFileTypeImage = R.drawable.file_type_document;
            }
            mParentFileAbsolutePath = getParentPath(path);
            mFileLastModifiedDate = getLastModifiedDate(mFile);
            mFileLastModifiedTime = getLastModifiedTime(mFile);
        }
    }

    // 是否为隐藏文件
    public boolean isHideFileType(){
        if(mFileName.startsWith(".")){
            return true;
        }

        return false;
    }

    public int getFileTypeImage() {
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

    public String getFileSize() {
        return mFileSize;
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

    /**
     * 获取文件夹中包含多少子文件
     */
    public int getChildFilesCount(Context context){
        boolean canShowHideFile = PreferenceUtils.canShowHideFile(context);

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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(lastChangeTime);
    }

    /**
     * 获取文件/文件夹的最后修改时间
     */
    private String getLastModifiedTime(File file){
        long lastChangeTime = file.lastModified();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(lastChangeTime);
    }

    /*
     * 根据文件名（包含路径）获取文件的MIME类型
     */
    public String getMimeType(String path){
        if((null == path) || (path.endsWith(".")) || (!path.contains("."))){
            return null;
        }

        int index = path.lastIndexOf(".");
        if (index != -1) {
            String extension = path.substring(index + 1).toLowerCase(Locale.US);
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        return null;
    }

    static class NameComparator implements Comparator{
        private boolean mIsFolderFirst = true;
        private boolean mIsAsc = true;

        public NameComparator(boolean isFolderFirst, boolean isAsc){
            mIsFolderFirst = isFolderFirst;
            mIsAsc = isAsc;
        }

        @Override
        public int compare(Object o1, Object o2) {
            boolean b1 = ((FileInfo)o1).isFolder();
            boolean b2 = ((FileInfo)o2).isFolder();

            if(b1 == b2) {
                String s1 = ((FileInfo) o1).getFileName().toUpperCase();
                String s2 = ((FileInfo) o2).getFileName().toUpperCase();

                if (mIsAsc) {
                    return s1.compareTo(s2);
                } else {
                    return s2.compareTo(s1);
                }
            }else{
                if(mIsFolderFirst){
                    if(b1){
                        return -1;
                    }
                    return 1;
                }else{
                    if(b1){
                        return 1;
                    }
                    return -1;
                }
            }
        }
    }
}
