package com.matrixzoo.flume.dao;

import com.alibaba.fastjson.JSON;
import com.matrixzoo.flume.constant.StateName;
import com.matrixzoo.flume.entity.JobLogs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class JobStatusDao {

    private JobLogsDao jobLogsDao;

    @Autowired
    public JobStatusDao(JobLogsDao jobLogsDao) {
        this.jobLogsDao = jobLogsDao;
    }

    public void updateExecState(String agentName, StateName execState, Map counterInfos, String errorLogs) {
        JobLogs execTaskInfo = jobLogsDao.findOne(agentName);
        if (execTaskInfo == null) execTaskInfo = new JobLogs();
        execTaskInfo.setAgentName(agentName);
        execTaskInfo.setExecState(execState);
        if (counterInfos != null) execTaskInfo.setCounterInfos(counterInfos);
        if (errorLogs != null) execTaskInfo.setErrorLogs(errorLogs);
        jobLogsDao.save(execTaskInfo);
    }

    /**
     * 任务启动中
     */
    public void execStarting(String agentName) {
        this.updateExecState(agentName, StateName.STARTING, null, null);
    }

    /**
     * 任务运行中
     */
    public void execRunning(String agentName) {
        this.updateExecState(agentName, StateName.RUNNING, null, null);
    }

    /**
     * 任务手动停止
     */
    public void execStoped(String agentName) {
        this.updateExecState(agentName, StateName.IDLE, null, null);
    }

    public void execStoped(String agentName, Map<String, Map<String, String>> counterInfos) {
        this.updateExecState(agentName, StateName.IDLE, counterInfos, null);
    }

    /**
     * 停止中
     */
    public void execStopping(String agentName) {
        this.updateExecState(agentName, StateName.SHUTTING_DOWN, null, null);
    }

    /**
     * 停止中
     */
    public void execStopping(String agentName, Map<String, Map<String, String>> counterInfos) {
        this.updateExecState(agentName, StateName.SHUTTING_DOWN, counterInfos, null);
    }

    /**
     * 运行失败
     */
    public void execFailed(String agentName) {
        this.updateExecState(agentName, StateName.ERROR, null, null);
    }

    /**
     * 运行失败
     */
    public void execFailed(String agentName, List<String> errors) {
        String errorLogs = JSON.toJSONString(errors);
        this.updateExecState(agentName, StateName.ERROR, null, errorLogs);
    }

    /**
     * 运行失败
     */
    public void execFailed(String agentName, Map<String, Map<String, String>> counterInfos, List<String> errors) {
        String errorLogs = JSON.toJSONString(errors);
        this.updateExecState(agentName, StateName.ERROR, counterInfos, errorLogs);
    }

    /**
     * 运行成功
     */
    public void execSuccess(String agentName) {
        this.updateExecState(agentName, StateName.SUCCESS, null, null);
    }

    public void execSuccess(String agentName, Map<String, Map<String, String>> counterInfos) {
        this.updateExecState(agentName, StateName.SUCCESS, counterInfos, null);
    }

    public boolean isRunning(String agentName) {
        JobLogs taskInfo = jobLogsDao.findOne(agentName);
        if (taskInfo != null) {
            StateName execState = taskInfo.getExecState();
            if (this.isRunning(taskInfo)
                    || StateName.STARTING.equals(execState)
                    || StateName.SHUTTING_DOWN.equals(execState)) {
                return true;
            } else {
                return false;
            }
        } else return false;
    }

    public boolean isRunning(JobLogs taskInfo) {
        StateName execState = taskInfo.getExecState();
        if (StateName.RUNNING.equals(execState)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除任务
     */
    public void execDrop(String agentName) throws Exception {
        if (!this.isRunning(agentName)) {
            jobLogsDao.delete(agentName);
        } else {
            throw new IllegalArgumentException("Job is running,stop it first!");
        }
    }

    /**
     * 删除任务
     */
    public void execDrop(List<String> agentNames) throws Exception {
        List<String> shouldRemove = new ArrayList<>();
        for (String agentName : agentNames) {
            if (!this.isRunning(agentName)) {
                shouldRemove.add(agentName);
            }
        }
        Iterable<JobLogs> shouldRemoveIt = jobLogsDao.findAll(shouldRemove);
        jobLogsDao.delete(shouldRemoveIt);

    }

    public Iterable<JobLogs> listAll() throws Exception {
        return jobLogsDao.findAll();
    }

    public void dropInfos(Iterable<JobLogs> jobs) {
        jobLogsDao.delete(jobs);
    }
}
