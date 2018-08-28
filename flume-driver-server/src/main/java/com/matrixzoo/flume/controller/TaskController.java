package com.matrixzoo.flume.controller;

import com.matrixzoo.flume.entity.JobPorperties;
import com.matrixzoo.flume.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping("/job/register")
    public void registerJob(@RequestBody JobPorperties jobPorperties) throws Exception {
        taskService.registerJob(jobPorperties);
    }

    @GetMapping("/job/infos")
    public JobPorperties getJobInfos(@RequestParam(value = "agentName") String agentName) throws Exception {
        return taskService.getJobInfos(agentName);
    }

    @GetMapping("/job/agents")
    public List<String> getAgentNames() throws Exception {
        return taskService.getAgentNames();
    }

    @DeleteMapping("/job/stop")
    public void stopJob(String agentName) throws Exception {
        taskService.stopJob(agentName);
    }

}
