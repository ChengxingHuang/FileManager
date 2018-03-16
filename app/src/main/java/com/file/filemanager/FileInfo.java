package com.file.filemanager;

import android.content.ComponentName;
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
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by huang on 2017/8/4.
 */

public class FileInfo {
    public static final String TAG = "FileInfo";

    public static final String UNIT_B = "B";
    public static final String UNIT_KB = "KB";
    public static final String UNIT_MB = "MB";
    public static final String UNIT_GB = "GB";
    public static final String UNIT_TB = "TB";

    private static final int UNIT_INTERVAL = 1024;
    private static final double ROUNDING_OFF = 0.005;
    private static final int DECIMAL_NUMBER = 100;
    private static final HashMap<String, Integer> sCustomDrawableIdsMap = new HashMap<String, Integer>();

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
                String mimeType = getMimeType(mFileAbsolutePath);

                if(sCustomDrawableIdsMap.containsKey(mimeType)){
                    mFileTypeImage = mContext.getResources().getDrawable(sCustomDrawableIdsMap.get(mimeType), null);
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

    // TODO: 2017/10/12   文件的MIME类型对应的图标，如果需要添加，添加在这里即可
    public static void init(){
        // Audio:flac/mp3/mid/ogg/mp4a/wav/wma
        registerMimeType(R.drawable.file_type_audio,
                "application/x-flac",
                "audio/mpeg",
                "audio/midi",
                "audio/ogg",
                "audio/mp4",
                "audio/wav",
                "audio/x-ms-wma");

        // Image:bmp/jpg/jpeg/gif/png/tif/tiff/tga/psd
        registerMimeType(R.drawable.file_type_image,
                "image/x-ms-bmp",
                "image/jpeg",
                "image/gif",
                "image/png",
                "image/tiff",
                "image/x-targa",
                "image/vnd.adobe.photoshop");

        // Video:avi/dv/mp4/mpeg/mpg/mov/wm/flv/mkv
        registerMimeType(R.drawable.file_type_video,
                "video/x-msvideo",
                "video/x-dv",
                "video/mp4",
                "video/mpeg",
                "video/quicktime",
                "video/x-ms-wmv",
                "video/x-flv",
                "video/x-matroska");

        // Text:txt
        registerMimeType(R.drawable.file_type_txt, "text/plain");

        // Source code
        registerMimeType(R.drawable.file_type_codes,
                "application/rdf+xml",
                "application/rss+xml",
                "application/x-object",
                "application/xhtml+xml",
                "text/css",
                "text/html",
                "text/xml",
                "text/x-c++hdr",
                "text/x-c++src",
                "text/x-chdr",
                "text/x-csrc",
                "text/x-dsrc",
                "text/x-csh",
                "text/x-haskell",
                "text/x-java",
                "text/x-literate-haskell",
                "text/x-pascal",
                "text/x-tcl",
                "text/x-tex",
                "application/x-latex",
                "application/x-texinfo",
                "application/atom+xml",
                "application/ecmascript",
                "application/json",
                "application/javascript",
                "application/xml",
                "text/javascript",
                "application/x-javascript");

        // Compressed:gz/tgz/bz/bz2/tbz/zip/rar/tar/7z
        registerMimeType(R.drawable.file_type_zip,
                "application/x-gzip",
                "application/x-bzip2",
                "application/zip",
                "application/x-rar",
                "application/x-tar",
                "application/x-7z-compressed");

        // Contact
        registerMimeType(R.drawable.file_type_vcf,
                "text/x-vcard",
                "text/vcard");

        // PDF
        registerMimeType(R.drawable.file_type_pdf, "application/pdf");

        // PPT
        registerMimeType(R.drawable.file_type_ppt,
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.openxmlformats-officedocument.presentationml.template",
                "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
                "application/vnd.stardivision.impress",
                "application/vnd.sun.xml.impress",
                "application/vnd.sun.xml.impress.template",
                "application/x-kpresenter",
                "application/vnd.oasis.opendocument.presentation");

        // Excel
        registerMimeType(R.drawable.file_type_ppt,
                "application/vnd.oasis.opendocument.spreadsheet",
                "application/vnd.oasis.opendocument.spreadsheet-template",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
                "application/vnd.stardivision.calc",
                "application/vnd.sun.xml.calc",
                "application/vnd.sun.xml.calc.template",
                "application/x-kspread",
                "text/comma-separated-values");

        // Word
        registerMimeType(R.drawable.file_type_word,
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
                "application/vnd.oasis.opendocument.text",
                "application/vnd.oasis.opendocument.text-master",
                "application/vnd.oasis.opendocument.text-template",
                "application/vnd.oasis.opendocument.text-web",
                "application/vnd.stardivision.writer",
                "application/vnd.stardivision.writer-global",
                "application/vnd.sun.xml.writer",
                "application/vnd.sun.xml.writer.global",
                "application/vnd.sun.xml.writer.template",
                "application/x-abiword",
                "application/x-kword",
                "text/markdown");
    }

    private static void registerMimeType(int resId, String... mimeTypes) {
        for (String type : mimeTypes) {
            sCustomDrawableIdsMap.put(type, resId);
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
        if(mFileName.startsWith(".")){
            return true;
        }

        return false;
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
