package com.matrixzoo.flume.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.matrixzoo.flume.FlumeJobRunner;
import com.matrixzoo.flume.constant.StateName;
import com.matrixzoo.flume.dao.JobStatusDao;
import com.matrixzoo.flume.entity.JobLogs;
import com.matrixzoo.flume.entity.JobPorperties;
import com.matrixzoo.flume.service.InfoService;
import com.matrixzoo.flume.utils.ScheduledMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class InfoServiceImpl implements InfoService {
    final private static Logger LOG = LoggerFactory.getLogger(InfoServiceImpl.class);
    final private StaticApplicationContext staticApplicationContext = ScheduledMonitor.getStaticApplicationContext();

    private JobStatusDao jobStatusDao;

    @Autowired
    public InfoServiceImpl(JobStatusDao jobStatusDao) {
        this.jobStatusDao = jobStatusDao;
    }

    @Override
    public List<String> getAgentNames() throws Exception {
        String[] agentNameArr = staticApplicationContext.getBeanNamesForType(FlumeJobRunner.class);
        return Arrays.asList(agentNameArr);
    }

    @Override
    public JobPorperties getJobInfos(String agentName) throws Exception {
        if (staticApplicationContext.containsBean(agentName)) {
            FlumeJobRunner flumeJobRunner = staticApplicationContext.getBean(agentName, FlumeJobRunner.class);
            JobPorperties jobPorperties = flumeJobRunner.getJobPorperties();
            Map<String, Map<String, String>> jmxProperties = flumeJobRunner.getAllMBeans();
            /*Map<String, Map<String, String>> lifeCycleCounters = new HashMap<>();
            for (String key : jmxProperties.keySet()) {
                String[] lifeCycle = StringUtils.splitPreserveAllTokens(key, ".");
                String nameNow = lifeCycle[1];
                String agentNameNow = nameNow.split(FlumeJobRunner.SPL)[0];
                if (agentName.equals(agentNameNow)) {
                    lifeCycleCounters.put(key, jmxProperties.get(key));
                }
            }*/
            jobPorperties.setLifeCycleCounters(jmxProperties);
            return jobPorperties;
        }
        return null;
    }

    @Override
    public List<JobLogs> listJobsInfos() throws Exception {
        Map<String, FlumeJobRunner> beans = staticApplicationContext.getBeansOfType(FlumeJobRunner.class);
        List<String> runningAgentNames = this.getAgentNames();
        List<JobLogs> runningList = Lists.newArrayList();
        for (String agentName : beans.keySet()) {
            FlumeJobRunner flumeJobRunner = beans.get(agentName);
            JobLogs jobLogs = new JobLogs();
            jobLogs.setAgentName(agentName);
            jobLogs.setCounterInfos(flumeJobRunner.getAllMBeans());
            jobLogs.setExecState(StateName.RUNNING);
            runningList.add(jobLogs);
        }
        Iterable<JobLogs> jobs = jobStatusDao.listAll();
        List<JobLogs> list = new ArrayList<>();
        jobs.forEach(jobLogs -> {
            if (!runningAgentNames.contains(jobLogs.getAgentName()))
                list.add(jobLogs);
        });
        list.addAll(runningList);
        return list;
    }

    @Override
    public List<JobLogs> cleanCachedInfos() throws Exception {
        Iterable<JobLogs> jobs = jobStatusDao.listAll();
        List<JobLogs> list = new ArrayList<>();
        jobs.forEach(jobLogs -> {
            list.add(jobLogs);
        });
        jobStatusDao.dropInfos(jobs);
        return list;
    }
}
