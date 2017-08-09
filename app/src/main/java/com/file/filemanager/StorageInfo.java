package com.file.filemanager;

/**
 * Created by huang on 2017/8/7.
 */

public class StorageInfo {
    private int mStorageTypeImage;
    private String mStorageName;
    private String mStorageAvailable;
    private String mStorageTotal;

    public StorageInfo(int image, String name, String available, String total){
        mStorageTypeImage = image;
        mStorageName = name;
        mStorageAvailable = available;
        mStorageTotal = total;
    }

    public int getStorageTypeImage() {
        return mStorageTypeImage;
    }

    public String getStorageName() {
        return mStorageName;
    }

    public String getStorageAvailable() {
        return mStorageAvailable;
    }

    public String getStorageTotal() {
        return mStorageTotal;
    }
}
