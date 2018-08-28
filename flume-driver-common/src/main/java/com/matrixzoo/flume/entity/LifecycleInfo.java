package com.matrixzoo.flume.entity;

import java.util.HashMap;

public class LifecycleInfo extends HashMap<String, String> {

    public String getNameSpace() {
        return this.get("nameSpace");
    }

    public void setNameSpace(String nameSpace) {
        this.put("nameSpace", nameSpace);
    }

    public String getType() {
        return this.get("type");
    }

    public void setType(String type) {
        this.put("type", type);
    }
}
