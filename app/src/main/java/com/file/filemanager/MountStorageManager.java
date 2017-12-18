package com.file.filemanager;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by huang on 2017/9/21.
 */

public class MountStorageManager {
    public static final String TAG = "MountStorageManager";

    private static MountStorageManager sInstance = new MountStorageManager();
    private ArrayList<MountStorage> mMountStorageList = new ArrayList<MountStorage>();

    public static class MountStorage {
        public String mDescription;
        public String mPath;
        public boolean mRemovable;
        public boolean mIsMounted;
        public String mAvailableSpace;
        public String mTotalSpace;
    }

    private MountStorageManager(){

    }

    public static MountStorageManager getInstance(){
        return sInstance;
    }

    public void init(Context context){
        StorageManager storageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
        mMountStorageList.clear();

        try {
            Method getVolumePaths = storageManager.getClass().getMethod("getVolumeList");
            StorageVolume[] volumes = (StorageVolume[]) getVolumePaths.invoke(storageManager);

            for(StorageVolume volume : volumes){
                MountStorage mountStorage = new MountStorage();
                File path = Utils.getReflectFile(volume, "mPath");

                mountStorage.mDescription = getStorageDescription(context, volume);
                mountStorage.mPath = path.toString();
                mountStorage.mIsMounted = Environment.MEDIA_MOUNTED.equals(Utils.getReflectString(volume, "mState"));
                mountStorage.mRemovable = Utils.getReflectBoolean(volume, "mRemovable");
                mountStorage.mAvailableSpace = Utils.getStorageAvailableSpace(context, mountStorage.mPath);
                mountStorage.mTotalSpace = Utils.getStorageTotalSpace(context, mountStorage.mPath);

                Log.d(TAG, "mDescription = " + mountStorage.mDescription + ", mPath = " + mountStorage.mPath
                            + ", mIsMounted = " + mountStorage.mIsMounted + ", mRemovable = " + mountStorage.mRemovable
                            + ", mAvailableSpace = " + mountStorage.mAvailableSpace + ", mTotalSpace = " + mountStorage.mTotalSpace);

                //已经mount并且存在子目录的StorageVolume才添加到list中
                //ps:vivo x7测试，包含otg，但是为空
                if(path.list().length > 0 && mountStorage.mIsMounted) {
                    mMountStorageList.add(mountStorage);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ArrayList<MountStorage> getMountStorageList(){
        return mMountStorageList;
    }

    private String getStorageDescription(Context context, StorageVolume volume){
        String description = null;

        int descriptionId = Utils.getReflectInt(volume, "mDescriptionId");
        if(-1 != descriptionId){
            description = context.getResources().getString(descriptionId);
        }else{
            description = Utils.getReflectString(volume, "mDescription");
        }

        return description;
    }
}
