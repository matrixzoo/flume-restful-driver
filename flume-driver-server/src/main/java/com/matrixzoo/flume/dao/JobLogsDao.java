package com.matrixzoo.flume.dao;

import com.matrixzoo.flume.entity.JobLogs;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobLogsDao extends CrudRepository<JobLogs, String> {
}
