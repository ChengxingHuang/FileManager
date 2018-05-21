package com.file.filemanager.Service;

import android.content.ContentResolver;

import java.util.List;

/**
 * Created by huang on 2018/4/1.
 */

public interface FileOperator {
    int TASK_PASTE_ID = 0x01;
    int TASK_SHOW_FILES_ID = 0x02;

    void pasteFile(List<String> srcPaths, String dstPath, FileOperatorListener listener);
    void deleteFile(List<String> deletePaths, FileOperatorListener listener);
    void createFolder(String newFilePath, FileOperatorListener listener);
    void showFile(String filePath, FileOperatorListener listener);
    void sortFile(FileOperatorListener listener);
    void searchFile(String searchName, String searchPath, ContentResolver resolver, FileOperatorListener listener);
    void renameFile(String oldPath, String newPath, FileOperatorListener listener);
    void cancelTask(int taskId);
}
