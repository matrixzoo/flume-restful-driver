package com.matrixzoo.test;

import com.alibaba.fastjson.JSON;
import com.matrixzoo.flume.FlumeJobRunner;
import com.matrixzoo.flume.entity.ChannelInfo;
import com.matrixzoo.flume.entity.JobPorperties;
import com.matrixzoo.flume.entity.LifecycleInfo;
import org.apache.flume.sink.RollingFileSink;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class UtilTest {

    private CloseableHttpClient httpclient = HttpClients.createDefault();

    @Test
    public void startRestJob() throws IOException {
        JobPorperties props = new UtilTest().setProps();
        String json = JSON.toJSONString(props);
        System.out.println(json);

        HttpPost httppost = new HttpPost("http://localhost:8100/job/register");
        httppost.addHeader("Content-type", "application/json");
        httppost.addHeader("X-Requested-By", "user");
        httppost.setEntity(new StringEntity(json, "UTF-8"));
        CloseableHttpResponse httpResponse = httpclient.execute(httppost);
        HttpEntity resEntity = httpResponse.getEntity();


    }

    private JobPorperties setProps() {
        String agentName = "agent1";
        JobPorperties jobPorperties = new JobPorperties();
        jobPorperties.setAgentName(agentName);

        LifecycleInfo source = new LifecycleInfo();
        source.setNameSpace("sr1");
        source.setType("spooldir");
        source.put("spoolDir", "D:\\opt\\storm-topology");
        jobPorperties.setSource(source);

        List<ChannelInfo> channels = new ArrayList<>();
        ChannelInfo channelInfo = new ChannelInfo();
        LifecycleInfo channel = new LifecycleInfo();
        channel.setNameSpace("ch1");
        channel.setType("file");
        String logfileURL = "flumeConf";
        String sourcePath = Paths.get(logfileURL, agentName, "checkpoint").toString();
        String dataPath = Paths.get(logfileURL, agentName, "tmp").toString();
        channel.put("checkpointDir", sourcePath);
        channel.put("dataDirs", dataPath);
        channelInfo.setChannel(channel);

        List<LifecycleInfo> sinks = new ArrayList<>();
        LifecycleInfo sink = new LifecycleInfo();
        sink.setType(RollingFileSink.class.getName());
        sink.setNameSpace("sink1");
        sink.put("sink.directory", "D:\\opt\\output");
        sinks.add(sink);
        channelInfo.setSinks(sinks);
        channels.add(channelInfo);
        jobPorperties.setChannels(channels);
        System.out.println(JSON.toJSONString(jobPorperties));
        return jobPorperties;
    }

    @Test
    public void testPropertis() {
        FlumeJobRunner flumeJobRunner = new FlumeJobRunner();
        flumeJobRunner.setJobPorperties(setProps());
        Properties properties = flumeJobRunner.generateProperties();
        String json = JSON.toJSONString(properties);
        System.out.println(json);
    }
}
