package com.dominicfeliton.worldwidechat.util;

import com.dominicfeliton.worldwidechat.WorldwideChat;

public abstract class GenericRunnable implements Cloneable, Runnable {

    private volatile boolean isCancelled = false;
    private CommonTask task;
    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();
    private String name = "";

    @Override
    public void run() {
        if (isCancelled) {
            // TODO: Logging could be platform-specific
            refs.debugMsg("Task " + name +  " is already cancelled. Exiting.");
            task.cancel();
            return;
        }

        execute();
    }

    public void cancel() {
        isCancelled = true;
        if (task != null) {
            refs.debugMsg("Cancelling task " + name + "...");
            task.cancel();
        }
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setTask(CommonTask task) {
        this.task = task;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public CommonTask getTask() {
        return task;
    }

    protected abstract void execute(); // User-defined task logic

    @Override
    public GenericRunnable clone() {
        try {
            GenericRunnable cloned = (GenericRunnable) super.clone();
            cloned.isCancelled = false;
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}