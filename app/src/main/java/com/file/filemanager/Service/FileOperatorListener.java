package com.file.filemanager.Service;

import com.file.filemanager.Task.TaskInfo;

/**
 * Created by huang on 2018/4/1.
 */

public interface FileOperatorListener {
    void onTaskPrepare();
    void onTaskProgress(TaskInfo taskInfo);
    void onTaskResult(TaskInfo taskInfo);
}
