package com.damon.test1;

import java.io.Serializable;

/**
 * 功能：
 *
 * Created by ZhouJW on 2015/6/18 17:01.
 */
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    public Task() {
    }

    public Task(String name) {

        this.name = name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getName() {

        return name;
    }
}
