package com.matrixzoo.flume.service;

import com.matrixzoo.flume.entity.JobPorperties;

import java.util.List;

public interface TaskService {
    void registerJob(JobPorperties jobPorperties) throws Exception;

    JobPorperties getJobInfos(String agentName) throws Exception;

    List<String> getAgentNames() throws Exception;

    void stopJob(String agentName) throws Exception;
}
