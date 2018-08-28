package com.matrixzoo.flume;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.flume.CounterGroup;
import org.apache.flume.conf.FlumeConfiguration;
import org.apache.flume.lifecycle.LifecycleAware;
import org.apache.flume.lifecycle.LifecycleState;
import org.apache.flume.node.PollingPropertiesFileConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PollingConfigurationProvider extends PropertiesConfigurationProvider
        implements LifecycleAware {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(PollingPropertiesFileConfigurationProvider.class);

    private final EventBus eventBus;
    private final Properties properties;
    private final int interval;
    private final CounterGroup counterGroup;
    private LifecycleState lifecycleState;

    private ScheduledExecutorService executorService;

    public PollingConfigurationProvider(String agentName,
                                        Properties properties, EventBus eventBus, int interval) {
        super(agentName, properties);
        this.eventBus = eventBus;
        this.properties = properties;
        this.interval = interval;
        counterGroup = new CounterGroup();
        lifecycleState = LifecycleState.IDLE;
    }

    private static final String DEFAULT_PROPERTIES_IMPLEMENTATION = "java.util.Properties";

    @Override
    public FlumeConfiguration getFlumeConfiguration() {
        return new FlumeConfiguration(toMap(properties));
    }

    @Override
    public void start() {
        LOGGER.info("Configuration provider starting");

        Preconditions.checkState(properties != null,
                "The parameter file must not be null");

        executorService = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setNameFormat("conf-file-poller-%d")
                        .build());

        PollingConfigurationProvider.FileWatcherRunnable fileWatcherRunnable =
                new PollingConfigurationProvider.FileWatcherRunnable(properties, counterGroup);

        executorService.scheduleWithFixedDelay(fileWatcherRunnable, 0, interval,
                TimeUnit.SECONDS);

        lifecycleState = LifecycleState.START;

        LOGGER.debug("Configuration provider started");
    }

    @Override
    public void stop() {
        LOGGER.info("Configuration provider stopping");

        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                LOGGER.debug("Waiting for file watcher to terminate");
            }
        } catch (InterruptedException e) {
            LOGGER.debug("Interrupted while waiting for file watcher to terminate");
            Thread.currentThread().interrupt();
        }
        lifecycleState = LifecycleState.STOP;
        LOGGER.debug("Configuration provider stopped");
    }

    @Override
    public synchronized LifecycleState getLifecycleState() {
        return lifecycleState;
    }


    @Override
    public String toString() {
        return "{ props:" + properties + " counterGroup:" + counterGroup + "  provider:"
                + getClass().getCanonicalName() + " agentName:" + getAgentName() + " }";
    }

    public class FileWatcherRunnable implements Runnable {

        private final Properties properties;
        private final CounterGroup counterGroup;

        public void setReload(boolean reload) {
            this.reload = reload;
        }

        private boolean reload = true;

        public FileWatcherRunnable(Properties properties, CounterGroup counterGroup) {
            super();
            this.properties = properties;
            this.counterGroup = counterGroup;
        }

        @Override
        public void run() {
            LOGGER.debug("Checking file:{} for changes", properties);
            counterGroup.incrementAndGet("file.checks");
            if (reload) {
                LOGGER.info("Reloading configuration.");
                counterGroup.incrementAndGet("file.loads");
                reload = false;
                try {
                    eventBus.post(getConfiguration());
                } catch (Exception e) {
                    LOGGER.error("Failed to load configuration data. Exception follows.",
                            e);
                } catch (NoClassDefFoundError e) {
                    LOGGER.error("Failed to start agent because dependencies were not " +
                            "found in classpath. Error follows.", e);
                } catch (Throwable t) {
                    // caught because the caller does not handle or log Throwables
                    LOGGER.error("Unhandled error", t);
                }
            }
        }
    }
}
