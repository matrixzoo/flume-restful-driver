package com.matrixzoo.flume;

import org.apache.flume.conf.FlumeConfiguration;
import org.apache.flume.node.AbstractConfigurationProvider;

import java.util.Properties;

public class PropertiesConfigurationProvider extends
        AbstractConfigurationProvider {
    private final Properties properties;

    public PropertiesConfigurationProvider(String agentName, Properties properties) {
        super(agentName);
        this.properties = properties;
    }

    @Override
    public FlumeConfiguration getFlumeConfiguration() {
        return new FlumeConfiguration(toMap(properties));
    }
}
