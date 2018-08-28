package com.matrixzoo.flume.entity;

import java.util.List;
import java.util.Map;

public class JobPorperties {
    private String agentName;
    private LifecycleInfo source;
    private LifecycleInfo interceptor;
    private List<ChannelInfo> channels;
    private Map<String, Map<String, String>> lifeCycleCounters;


    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public LifecycleInfo getSource() {
        return source;
    }

    public void setSource(LifecycleInfo source) {
        this.source = source;
    }

    public LifecycleInfo getInterceptor() {
        return interceptor;
    }

    public void setInterceptor(LifecycleInfo interceptor) {
        this.interceptor = interceptor;
    }

    public List<ChannelInfo> getChannels() {
        return channels;
    }

    public void setChannels(List<ChannelInfo> channels) {
        this.channels = channels;
    }

    public Map<String, Map<String, String>> getLifeCycleCounters() {
        return lifeCycleCounters;
    }

    public void setLifeCycleCounters(Map<String, Map<String, String>> lifeCycleCounters) {
        this.lifeCycleCounters = lifeCycleCounters;
    }
}
