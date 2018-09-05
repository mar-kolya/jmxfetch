package org.datadog.jmxfetch;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.util.Properties;
import org.apache.log4j.Logger;

public class Agent {

    public static final String PROPERTY_PREFIX = "datadog.jmxfetch.";
    public static final String PROPERTY_LOG_LEVEL = PROPERTY_PREFIX + "logLevel";
    public static final String PROPERTY_LOG_LOCATION = PROPERTY_PREFIX + "logLocation";
    public static final String PROPERTY_CONF_DIR = PROPERTY_PREFIX + "confDir";
    public static final String PROPERTY_TMP_DIR = PROPERTY_PREFIX + "tmpDir";
    public static final String PROPERTY_REPORTER = PROPERTY_PREFIX + "reporter";
    public static final String PROPERTY_CHECK = PROPERTY_PREFIX + "check";
    public static final String PROPERTY_CHECK_PERIOD = PROPERTY_PREFIX + "checkPeriod";
    public static final String PROPERTY_AD_ENABLED = PROPERTY_PREFIX + "adEnabled";
    public static final String PROPERTY_AD_PIPE = PROPERTY_PREFIX + "adPipe";
    public static final String PROPERTY_STATUS_LOCATION = PROPERTY_PREFIX + "statusLocation";
    public static final String PROPERTY_EXIT_FILE_LOCATION = PROPERTY_PREFIX + "exitFileLocation";
    public static final String PROPERTY_IPC_HOST = PROPERTY_PREFIX + "adIpcHost";
    public static final String PROPERTY_IPC_PORT = PROPERTY_PREFIX + "adIpcPort";

    private final static Logger LOGGER = Logger.getLogger(App.class.getName());

    /**
     * Main entry point into java agent. Called on JVM start up.
     */
    public static void premain(String agentArgs) {
        Properties properties = System.getProperties();
        Splitter colonSplitter = Splitter.on(":");

        final ImmutableList.Builder<String> args = ImmutableList.builder();
        args.add(AppConfig.ACTION_COLLECT);
        if (properties.containsKey(PROPERTY_LOG_LEVEL)) {
            args.add("--log_level");
            args.add(properties.getProperty(PROPERTY_LOG_LEVEL));
        }
        if (properties.containsKey(PROPERTY_LOG_LOCATION)) {
            args.add("--log_location");
            args.add(properties.getProperty(PROPERTY_LOG_LOCATION));
        }
        if (properties.containsKey(PROPERTY_CONF_DIR)) {
            args.add("--conf_directory");
            args.add(properties.getProperty(PROPERTY_CONF_DIR));
        }
        if (properties.containsKey(PROPERTY_TMP_DIR)) {
            args.add("--tmp_directory");
            args.add(properties.getProperty(PROPERTY_TMP_DIR));
        }
        if (properties.containsKey(PROPERTY_REPORTER)) {
            args.add("--reporter");
            args.add(properties.getProperty(PROPERTY_REPORTER));
        }
        if (properties.containsKey(PROPERTY_CHECK)) {
            for (String check : colonSplitter.split(properties.getProperty(PROPERTY_CHECK))) {
                args.add("--check");
                args.add(check);
            }
        }
        if (properties.containsKey(PROPERTY_CHECK_PERIOD)) {
            args.add("--check_period");
            args.add(properties.getProperty(PROPERTY_CHECK_PERIOD));
        }
        if (properties.containsKey(PROPERTY_AD_ENABLED)) {
            args.add("--ad_enabled");
        }
        if (properties.containsKey(PROPERTY_AD_PIPE)) {
            args.add("--ad_pipe");
            args.add(properties.getProperty(PROPERTY_AD_PIPE));
        }
        if (properties.containsKey(PROPERTY_STATUS_LOCATION)) {
            args.add("--status_location");
            args.add(properties.getProperty(PROPERTY_STATUS_LOCATION));
        }
        if (properties.containsKey(PROPERTY_EXIT_FILE_LOCATION)) {
            args.add("--exit_file_location");
            args.add(properties.getProperty(PROPERTY_EXIT_FILE_LOCATION));
        }
        if (properties.containsKey(PROPERTY_IPC_HOST)) {
            args.add("--ipc_host");
            args.add(properties.getProperty(PROPERTY_IPC_HOST));
        }
        if (properties.containsKey(PROPERTY_IPC_PORT)) {
            args.add("--ipc_port");
            args.add(properties.getProperty(PROPERTY_IPC_PORT));
        }

        // FIXME: what happens when JVM shutdowns?
        // FIXME: do we need to somehow limit amount of CPU eaten by JMXFetch
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        int result = App.run(args.build().toArray(new String[]{}));
                        LOGGER.error("jmx collector exited with result: " + result);
                    } catch (Exception e) {
                        LOGGER.error("Exception in jmx collector thread", e);
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new Error(e);
                    }
                }
            }
        });
        thread.setName("dd-jmx-collector");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Called when agent is started inside running JVM.
     */
    public static void agentmain(String agentArgs) {
        premain(agentArgs);
    }
}
