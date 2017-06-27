package com.rmacd.rundeck.plugins;

public interface PluginConf {
    interface Key {
        String getDefaultValue();
    }
    String getStr(Key key);
//    String getStr(Key key, String defaultValue);
    Integer getInt(Key key);
//    int getInt(Key key, int defaultValue);
}
