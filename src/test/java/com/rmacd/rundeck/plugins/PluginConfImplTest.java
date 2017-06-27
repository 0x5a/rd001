package com.rmacd.rundeck.plugins;

import com.rmacd.rundeck.plugins.PluginConf;
import com.rmacd.rundeck.plugins.PluginConfImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Shows how this can be unit tested
 */
public class PluginConfImplTest {

    PluginConf conf;

    public PluginConfImplTest() {
        this.conf = PluginConfImpl.TEST;
    }

    @Test
    public void getStr() throws Exception {
        // this is not set in test properties so should return default
        assertEquals(conf.getStr(PluginConfImpl.Key.ZK_HOST), "localhost");
    }

}