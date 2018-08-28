package com.matrixzoo.flume.service.impl;

import com.matrixzoo.flume.FlumeJobRunner;
import com.matrixzoo.flume.entity.JobPorperties;
import com.matrixzoo.flume.service.TaskService;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.instrumentation.util.JMXPollUtil;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {
    private StaticApplicationContext staticApplicationContext = new StaticApplicationContext();

    @Override
    public void registerJob(JobPorperties jobPorperties) throws Exception {
        staticApplicationContext.registerSingleton(jobPorperties.getAgentName(), FlumeJobRunner.class);
        FlumeJobRunner flumeJobRunner = staticApplicationContext.getBean(jobPorperties.getAgentName(), FlumeJobRunner.class);
        flumeJobRunner.setJobPorperties(jobPorperties);
        flumeJobRunner.runJob();
    }

    @Override
    public JobPorperties getJobInfos(String agentName) throws Exception {
        if (staticApplicationContext.containsBean(agentName)) {
            FlumeJobRunner flumeJobRunner = staticApplicationContext.getBean(agentName, FlumeJobRunner.class);
            JobPorperties jobPorperties = flumeJobRunner.getJobPorperties();
            Map<String, Map<String, String>> jmxProperties = JMXPollUtil.getAllMBeans();
            Map<String, Map<String, String>> lifeCycleCounters = new HashMap<>();
            for (String key : jmxProperties.keySet()) {
                String[] lifeCycle = StringUtils.splitPreserveAllTokens(key, ".");
                String nameNow = lifeCycle[1];
                String agentNameNow = nameNow.split(FlumeJobRunner.SPL)[0];
                if (agentName.equals(agentNameNow)) {
                    lifeCycleCounters.put(key, jmxProperties.get(key));
                }
            }
            jobPorperties.setLifeCycleCounters(lifeCycleCounters);
            return jobPorperties;
        }
        return null;
    }

    @Override
    public List<String> getAgentNames() throws Exception {
        String[] agentNameArr = staticApplicationContext.getBeanNamesForType(FlumeJobRunner.class);
        return Arrays.asList(agentNameArr);
    }

    @Override
    public void stopJob(String agentName) throws Exception {
        if (staticApplicationContext.containsBean(agentName)) {
            FlumeJobRunner flumeJobRunner = staticApplicationContext.getBean(agentName, FlumeJobRunner.class);
            flumeJobRunner.stop();
            staticApplicationContext.removeBeanDefinition(agentName);
        }
    }
}
