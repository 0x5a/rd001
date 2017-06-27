package com.rmacd.rundeck.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

// based on pattern as outlined on SO#2503489

/**
 * enum used for loading up properties ... enum initialisation also allows
 * for this to be unit-testable (or for initialisation to be controlled
 * elsewhere).
 *
 * New properties should be defined in this class and you are required to
 * also provide a default value for that property.
 */
public enum PluginConfImpl implements PluginConf {
    // for now we are just loading one
    // set of properties but this could
    // easily be substituted in the JUnit
    // if need be
    INSTANCE("/application.properties"),
    TEST("/application-test.properties");

    private final Properties properties;

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfImpl.class);

    PluginConfImpl(String path) {
        properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream(path));
        } catch (IOException | NullPointerException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public String getStr(PluginConf.Key key){
        return (null == properties.getProperty(key.toString()) || properties.getProperty(key.toString()).isEmpty()) ? key.getDefaultValue() : properties.getProperty(key.toString());
    }

    @Override
    public Integer getInt(PluginConf.Key key){
        try {
            return Integer.parseInt(getStr(key));
        }
        catch (NumberFormatException e) {
            LOGGER.error(String.format("Cannot parse value '%s', exception %s", getStr(key), e.getMessage()));
        }
        return null;
    }

    /**
     * Here we list the Keys that we expect to be defined in
     * the application properties file ... plus defaults,
     * in case they are not found in the properties file.
     */
    public enum Key implements PluginConf.Key {
        ZK_HOST("zk.host", "localhost"),
        ZK_PORT("zk.port", "8080"),
        LOCK_DEFAULT_TIMEOUT("zk.lock.default.timeout", "600"),
        ZK_RESOURCE_PATH("zk.resource.path", "/rundeck/resources");

        private final String name;
        private final String defaultValue;

        Key(String name, String defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        @Override
        public String toString() { return name; }

        @Override
        public String getDefaultValue() { return defaultValue; }

    }
}
