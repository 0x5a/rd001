package com.rmacd.rundeck.plugins;

import org.apache.curator.framework.CuratorFramework;

public class ZKTest {


    public static void main(String[] args) {
        RDZKClientImpl client = new RDZKClientImpl();
        CuratorFramework myClient = client.client;
        System.out.println("Created client instance");
        myClient.getState();
    }
}
