package com.matrixzoo.flume.entity;

import java.util.List;

public class ChannelInfo {
    private LifecycleInfo channel;
    private List<LifecycleInfo> sinks;

    public LifecycleInfo getChannel() {
        return channel;
    }

    public void setChannel(LifecycleInfo channel) {
        this.channel = channel;
    }

    public List<LifecycleInfo> getSinks() {
        return sinks;
    }

    public void setSinks(List<LifecycleInfo> sinks) {
        this.sinks = sinks;
    }
}
