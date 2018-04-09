package com.file.filemanager.Service;

import java.util.List;

/**
 * Created by huang on 2018/4/1.
 */

public interface FileOperator {
    int TASK_PASTE_ID = 0x01;

    void pasteFile(List<String> srcPaths, String dstPath, FileOperatorListener listener);
    void deleteFile(List<String> deletePaths, FileOperatorListener listener);
    void createFolder(String newFilePath, FileOperatorListener listener);
    void showFile(String filePath, FileOperatorListener listener);
    void sortFile(FileOperatorListener listener);
    void searchFile(FileOperatorListener listener);
    void cancelTask(int taskId);
}
