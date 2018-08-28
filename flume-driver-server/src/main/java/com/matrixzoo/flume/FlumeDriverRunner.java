package com.matrixzoo.flume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FlumeDriverRunner {
    final private static Logger LOG = LoggerFactory.getLogger(FlumeDriverRunner.class);

    public static void main(String[] args) {
        try {
            SpringApplication springApplication = new SpringApplication(FlumeDriverRunner.class);
//            springApplication.setBanner(new TumblingBanner());
            springApplication.run(args);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
