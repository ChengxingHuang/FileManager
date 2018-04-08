package com.file.filemanager.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.file.filemanager.Task.BaseAsyncTask;
import com.file.filemanager.Task.DeleteTask;

import java.util.List;

public class FileManagerService extends Service {
    public FileManagerService() {
    }

    class FileOperatorBinder extends Binder implements FileOperator{

        @Override
        public void pasteFile(String srcPath, String dstPath, boolean isCut, FileOperatorListener listener) {

        }

        @Override
        public void deleteFile(List<String> deletePaths, FileOperatorListener listener) {
            BaseAsyncTask task = new DeleteTask(deletePaths, listener);
            task.execute();
        }

        @Override
        public void createFolder(String newFilePath, FileOperatorListener listener) {

        }

        @Override
        public void showFile(String filePath, FileOperatorListener listener) {

        }

        @Override
        public void sortFile(FileOperatorListener listener) {

        }

        @Override
        public void searchFile(FileOperatorListener listener) {

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new FileOperatorBinder();
    }
}
