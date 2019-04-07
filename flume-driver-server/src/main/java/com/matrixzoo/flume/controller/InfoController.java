package com.matrixzoo.flume.controller;

import com.matrixzoo.flume.entity.JobLogs;
import com.matrixzoo.flume.entity.JobPorperties;
import com.matrixzoo.flume.service.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class InfoController {

    private InfoService infoService;

    @Autowired
    public InfoController(InfoService infoService) {
        this.infoService = infoService;
    }

    @GetMapping("/job/agents")
    public List<String> getAgentNames() throws Exception {
        return infoService.getAgentNames();
    }

    @GetMapping("/job/infos")
    public JobPorperties getJobInfos(@RequestParam(value = "agentName") String agentName) throws Exception {
        return infoService.getJobInfos(agentName);
    }

    @GetMapping("/jobs/list")
    public List<JobLogs> listJobsInfos() throws Exception {
        return infoService.listJobsInfos();
    }

    @DeleteMapping("/jobs/clean")
    public List<JobLogs> cleanCachedInfos() throws Exception{
        return infoService.cleanCachedInfos();
    }

}
