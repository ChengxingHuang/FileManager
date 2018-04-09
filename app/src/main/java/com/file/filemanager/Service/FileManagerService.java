package com.file.filemanager.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.file.filemanager.Task.BaseAsyncTask;
import com.file.filemanager.Task.DeleteTask;
import com.file.filemanager.Task.PasteTask;

import java.util.List;

public class FileManagerService extends Service {
    public FileManagerService() {
    }

    class FileOperatorBinder extends Binder implements FileOperator{
        private BaseAsyncTask mPasteTask;

        @Override
        public void pasteFile(List<String> srcPaths, String dstPath, FileOperatorListener listener) {
            mPasteTask = new PasteTask(srcPaths, dstPath, listener);
            mPasteTask.execute();
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

        @Override
        public void cancelTask(int taskId) {
            if(TASK_PASTE_ID == taskId)
                mPasteTask.cancel(true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new FileOperatorBinder();
    }
}
