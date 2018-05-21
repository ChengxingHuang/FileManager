package com.file.filemanager.Service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.file.filemanager.Task.BaseAsyncTask;
import com.file.filemanager.Task.CreateFolderTask;
import com.file.filemanager.Task.DeleteTask;
import com.file.filemanager.Task.PasteTask;
import com.file.filemanager.Task.RenameTask;
import com.file.filemanager.Task.SearchTask;
import com.file.filemanager.Task.ShowFilesTask;
import com.file.filemanager.Task.SortTask;

import java.util.List;

public class FileManagerService extends Service {
    public FileManagerService() {
    }

    class FileOperatorBinder extends Binder implements FileOperator{
        private BaseAsyncTask mPasteTask;
        private BaseAsyncTask mShowFilesTask;

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
            BaseAsyncTask task = new CreateFolderTask(newFilePath, listener);
            task.execute();
        }

        @Override
        public void showFile(String filePath, FileOperatorListener listener) {
            mShowFilesTask = new ShowFilesTask(FileManagerService.this, filePath, listener);
            mShowFilesTask.execute();
        }

        @Override
        public void sortFile(FileOperatorListener listener) {
            BaseAsyncTask task = new SortTask(FileManagerService.this, listener);
            task.execute();
        }

        @Override
        public void searchFile(String searchName, String searchPath, ContentResolver resolver, FileOperatorListener listener){
            BaseAsyncTask task = new SearchTask(FileManagerService.this, searchName, searchPath, resolver, listener);
            task.execute();
        }

        @Override
        public void renameFile(String oldPath, String newPath, FileOperatorListener listener) {
            BaseAsyncTask task = new RenameTask(oldPath, newPath, listener);
            task.execute();
        }

        @Override
        public void cancelTask(int taskId) {
            switch (taskId){
                case TASK_PASTE_ID:
                    mPasteTask.cancel(true);
                    break;

                case TASK_SHOW_FILES_ID:
                    mShowFilesTask.cancel(true);
                    break;
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new FileOperatorBinder();
    }
}
