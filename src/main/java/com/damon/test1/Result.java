package com.damon.test1;

import java.io.Serializable;

/**
 * 功能：
 *
 * Created by ZhouJW on 2015/6/18 16:59.
 */
public class Result implements Serializable {

    private static final long serialVersionUID = 1L;

    private String taskName;

    private String result;

    public Result() {
    }

    public Result(String taskName, String result) {
        this.taskName = taskName;
        this.result = result;
    }

    public void setTaskName(String taskName) {

        this.taskName = taskName;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTaskName() {

        return taskName;
    }

    public String getResult() {
        return result;
    }
}
