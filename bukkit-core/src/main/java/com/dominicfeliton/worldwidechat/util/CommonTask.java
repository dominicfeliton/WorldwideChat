package com.dominicfeliton.worldwidechat.util;

public interface CommonTask {

    void cancel();

    boolean isCancelled();

    int getTaskId();

    Object getUnderlyingTask();  // New method to get the underlying task

}
