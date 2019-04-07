package com.matrixzoo.flume.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.matrixzoo.flume.constant.StateName;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Map;

@Entity
public class JobLogs {
    @Id
    private String agentName;
    /**
     * 状态
     */
    private StateName execState;

    @Column(columnDefinition = "TEXT")
    private String counterInfos;

    private String errorLogs;

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public StateName getExecState() {
        return execState;
    }

    public void setExecState(StateName execState) {
        this.execState = execState;
    }

    public Map getCounterInfos() {
        if (counterInfos == null) return null;
        else {
            JSONObject conterJSON = JSON.parseObject(counterInfos);
            return conterJSON;
        }
    }

    public void setCounterInfos(Map counterInfos) {
        this.counterInfos = JSON.toJSONString(counterInfos);
    }

    public String getErrorLogs() {
        return errorLogs;
    }

    public void setErrorLogs(String errorLogs) {
        this.errorLogs = errorLogs;
    }
}
