package com.matrixzoo.flume;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.matrixzoo.flume.entity.ChannelInfo;
import com.matrixzoo.flume.entity.JobPorperties;
import com.matrixzoo.flume.entity.LifecycleInfo;
import org.apache.flume.lifecycle.LifecycleAware;
import org.apache.flume.node.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 */
public class FlumeJobRunner {
    final private static Logger LOG = LoggerFactory.getLogger(FlumeJobRunner.class);
    final private static String DOT = ".";
    final public static String SPL = "_";
    final private static String SPACE = " ";
    final private static String SOURCES = "sources";
    final private static String CHANNEL = "channel";
    final private static String CHANNELS = "channels";
    final private static String SINKS = "sinks";
    final private static String INTERCEPTORS = "interceptors";
    private JobPorperties jobPorperties;
    private Application application;

    public void runJob() throws IOException {
        this.runFlumeJob(jobPorperties.getAgentName(), generateProperties(), false);
    }

    public Properties generateProperties() {
        Properties properties = new Properties();
        String agentName = jobPorperties.getAgentName();
        Preconditions.checkNotNull(agentName, "Agent name is undefined");
        List<ChannelInfo> channels = jobPorperties.getChannels();
        List<String> channelIds = new ArrayList<>();
        List<String> sinkIds = new ArrayList<>();
        for (ChannelInfo oneChannel : channels) {
            LifecycleInfo channel = oneChannel.getChannel();
            String channelName = channel.getNameSpace();
            String channelID = agentName + SPL + channelName;
            channelIds.add(channelID);
            List<String> oneSinkIds = this.generateChannel(properties, channelID, oneChannel);
            sinkIds.addAll(oneSinkIds);
        }
        StringBuilder channelIdstr = new StringBuilder();
        for (int index = 0; index < channelIds.size(); index++) {
            channelIdstr.append(channelIds.get(index));
            if (index < channelIds.size() - 1) {
                channelIdstr.append(SPACE);
            }
        }
        String sourceId = this.generateSource(properties, channelIdstr.toString());
        properties.put(agentName + DOT + SOURCES, sourceId);
        properties.put(agentName + DOT + CHANNELS, channelIdstr.toString());

        StringBuilder sinkIdstr = new StringBuilder();
        for (int index = 0; index < sinkIds.size(); index++) {
            sinkIdstr.append(sinkIds.get(index));
            if (index < sinkIds.size() - 1) {
                sinkIdstr.append(SPACE);
            }
        }
        properties.put(agentName + DOT + SINKS, sinkIdstr.toString());
        return properties;
    }

    private String generateSource(Properties properties, String channelIds) {
        String agentName = jobPorperties.getAgentName();
        LifecycleInfo source = jobPorperties.getSource();
        String nameSpace = source.getNameSpace();
        Preconditions.checkNotNull(nameSpace, "Name space of source is undefined");
        String sourceId = agentName + SPL + nameSpace;
        String sourcePrefix = agentName + DOT + SOURCES + DOT + sourceId + DOT;
        properties.put(sourcePrefix + CHANNELS, channelIds);
        for (String key : source.keySet()) {
            properties.put(sourcePrefix + key, source.get(key));
        }
        properties.put(agentName + DOT + SOURCES, sourceId);
        LifecycleInfo interceptor = jobPorperties.getInterceptor();
        if (interceptor != null && !interceptor.isEmpty()) {
            String interceptorNameSpace = interceptor.getNameSpace();
            Preconditions.checkNotNull(interceptorNameSpace, "Name space of interceptor is undefined");
            String interceptorId = agentName + SPL + interceptorNameSpace;
            properties.put(sourcePrefix + INTERCEPTORS, interceptorId);
            String interceptorPrefix = sourcePrefix + INTERCEPTORS + DOT + interceptorId + DOT;
            for (String key : interceptor.keySet()) {
                properties.put(interceptorPrefix + key, interceptor.get(key));
            }
        }
        return sourceId;
    }

    private List<String> generateChannel(Properties properties, String channelId, ChannelInfo oneChannel) {
        String agentName = jobPorperties.getAgentName();
        String channelPrefix = agentName + DOT + CHANNELS + DOT + channelId + DOT;
        LifecycleInfo channel = oneChannel.getChannel();
        for (String key : channel.keySet()) {
            properties.put(channelPrefix + key, channel.get(key));
        }
        List<LifecycleInfo> sinks = oneChannel.getSinks();
        return this.generateSinks(properties, channelId, sinks);
    }

    /**
     * @param properties
     * @param channelId
     * @param sinks
     * @return sink IDs of specified channel
     */
    private List<String> generateSinks(Properties properties, String channelId, List<LifecycleInfo> sinks) {
        String agentName = jobPorperties.getAgentName();
        List<String> sinkIds = new ArrayList<>();
        for (LifecycleInfo oneSink : sinks) {
            String nameSpace = oneSink.getNameSpace();
            Preconditions.checkNotNull(nameSpace, "Name space of sink is undefined");
            String sinkId = agentName + SPL + nameSpace;
            sinkIds.add(sinkId);
            String sinkPrefix = agentName + DOT + SINKS + DOT + sinkId + DOT;
            properties.put(sinkPrefix + CHANNEL, channelId);
            for (String key : oneSink.keySet()) {
                properties.put(sinkPrefix + key, oneSink.get(key));
            }
        }
        return sinkIds;
    }

    public void runFlumeJob(String agentName, Properties properties, boolean reload) throws IOException {
        try {
            application = new Application();
            List<LifecycleAware> components = Lists.newArrayList();
            if (reload) {
                EventBus eventBus = new EventBus(agentName + "-event-bus");
                PollingConfigurationProvider configurationProvider =
                        new PollingConfigurationProvider(
                                agentName, properties, eventBus, 30);
                components.add(configurationProvider);
                application = new Application(components);
                eventBus.register(application);
            } else {
                PropertiesConfigurationProvider configurationProvider =
                        new PropertiesConfigurationProvider(agentName, properties);
                application = new Application();
                application.handleConfigurationEvent(configurationProvider.getConfiguration());
            }
            application.start();
            final Application appReference = application;
            Runtime.getRuntime().addShutdownHook(new Thread("agent-hook-" + agentName) {
                @Override
                public void run() {
                    appReference.stop();
                }
            });
        } catch (Exception e) {
            LOG.error("A fatal error occurred while running. Exception follows.", e);
            throw new IOException("A fatal error occurred while running. Exception follows.", e);
        }
    }

    public void stop() {
        if (application != null) application.stop();
    }

    public void getStatus(){

    }

    public JobPorperties getJobPorperties() {
        return jobPorperties;
    }

    public void setJobPorperties(JobPorperties jobPorperties) {
        this.jobPorperties = jobPorperties;
    }
}
