package com.matrixzoo.flume.controller;

import com.matrixzoo.flume.entity.JobPorperties;
import com.matrixzoo.flume.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TaskController {

    private TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/job/register")
    public void registerJob(@RequestBody JobPorperties jobPorperties) throws Exception {
        taskService.registerJob(jobPorperties);
    }

    @DeleteMapping("/job/stop")
    public void stopJob(String agentName) throws Exception {
        taskService.stopJob(agentName);
    }

}
