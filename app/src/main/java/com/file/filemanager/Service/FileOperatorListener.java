package com.file.filemanager.Service;

import com.file.filemanager.Task.TaskInfo;

/**
 * Created by huang on 2018/4/1.
 */

public interface FileOperatorListener {
    int ERROR_CODE_SUCCESS = 0x00;
    int ERROR_CODE_USER_CANCEL = 0x01;

    int ERROR_CODE_FILE_NOT_EXIST = 0x02;
    int ERROR_CODE_DELETE_FAIL = 0x03;
    int ERROR_CODE_DELETE_NO_PERMISSION = 0x04;

    void onTaskPrepare();
    void onTaskProgress(TaskInfo progressInfo);
    void onTaskResult(TaskInfo.ErrorInfo errorInfo);
}
