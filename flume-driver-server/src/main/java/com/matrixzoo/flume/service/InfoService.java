package com.matrixzoo.flume.service;

import com.matrixzoo.flume.entity.JobLogs;
import com.matrixzoo.flume.entity.JobPorperties;

import java.util.List;

public interface InfoService {

    List<String> getAgentNames() throws Exception;

    JobPorperties getJobInfos(String agentName) throws Exception;

    List<JobLogs> listJobsInfos() throws Exception;

    List<JobLogs> cleanCachedInfos() throws Exception;
}
