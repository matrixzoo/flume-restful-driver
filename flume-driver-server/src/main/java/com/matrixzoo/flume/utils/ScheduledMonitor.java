package com.matrixzoo.flume.utils;

import com.matrixzoo.flume.FlumeJobRunner;
import com.matrixzoo.flume.dao.JobStatusDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Configurable
@EnableScheduling
final public class ScheduledMonitor {
    final private static Logger LOG = LoggerFactory.getLogger(ScheduledMonitor.class);
    final private static StaticApplicationContext staticApplicationContext = new StaticApplicationContext();

    private JobStatusDao jobStatusDao;

    @Autowired
    public ScheduledMonitor(JobStatusDao jobStatusDao) {
        this.jobStatusDao = jobStatusDao;
    }

    @Scheduled(cron = "0 */1 *  * * * ")
    public void reportCurrentByCron() {
        Map<String, FlumeJobRunner> jobs = staticApplicationContext.getBeansOfType(FlumeJobRunner.class);
        for (String agentName : jobs.keySet()) {
            FlumeJobRunner agent = jobs.get(agentName);
            Map<String, Map<String, String>> counterInfo = agent.getAllMBeans();
            //清理已结束任务
            if (agent.isTaskEnd()) {
                try {
                    agent.stop();
                } catch (Exception e) {
                    LOG.warn(e.getMessage());
                }
                jobStatusDao.execStoped(agentName);
                staticApplicationContext.removeBeanDefinition(agentName);
            }
            //清理报错任务
            else if (agent.isJobError()) {
                jobStatusDao.execStopping(agentName, counterInfo);
                agent.stop();
                List<String> errors = agent.getStackTraceInfos();
                jobStatusDao.execFailed(agentName, errors);
                staticApplicationContext.removeBeanDefinition(agentName);
            }
            //停止主动停止了收集数据源的任务
            else if (agent.isSourcesStopped() && agent.isEmptySinks()) {
                jobStatusDao.execStopping(agentName, counterInfo);
                agent.stop();
                jobStatusDao.execSuccess(agentName);
                staticApplicationContext.removeBeanDefinition(agentName);
            }
        }
    }

    public static StaticApplicationContext getStaticApplicationContext() {
        return staticApplicationContext;
    }
}
