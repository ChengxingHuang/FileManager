package com.file.filemanager.Service;

import java.util.List;

/**
 * Created by huang on 2018/4/1.
 */

public interface FileOperator {
    void pasteFile(String srcPath, String dstPath, boolean isCut, FileOperatorListener listener);
    void deleteFile(List<String> deletePaths, FileOperatorListener listener);
    void createFolder(String newFilePath, FileOperatorListener listener);
    void showFile(String filePath, FileOperatorListener listener);
    void sortFile(FileOperatorListener listener);
    void searchFile(FileOperatorListener listener);
}
