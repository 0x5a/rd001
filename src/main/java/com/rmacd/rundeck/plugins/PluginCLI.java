package com.rmacd.rundeck.plugins;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.cs.FastCharsetProvider;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides helper utilities that can be executed on the CLI
 * to access the ZK instance
 */
public class PluginCLI {

    private static Logger LOGGER = LoggerFactory.getLogger(PluginCLI.class);

    public static void main(String[] args) throws StepException {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption("l", "list", false, "List all defined resources on ZooKeeper instance");
        options.addOption("h", "help", false, "Show application help");
//        options.addOption("d", "debug", false, "Send debug output to stdout");

        Option addResourceNameOpt = Option.builder("a")
                .longOpt("add")
                .desc("Add ZooKeeper resource by name")
                .hasArg()
                .argName("resourceName")
                .build();
        options.addOption(addResourceNameOpt);

        Option removeResourceNameOpt = Option.builder("d")
                .longOpt("remove")
                .desc("Remove ZooKeeper resource by name")
                .hasArg()
                .argName("resourceName")
                .build();
        options.addOption(removeResourceNameOpt);

        Option zkHostname = Option.builder("H")
                .longOpt("host")
                .desc("ZooKeeper hostname")
                .hasArg()
                .argName("host")
                .build();
        options.addOption(zkHostname);

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if (line.getOptions().length == 0 || line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "rdzk-cli", options);
            }

            if (line.hasOption("l")) {
                listResources();
            }

            if (line.hasOption("a")) {
                addResource(line.getOptionValue("a"));
            }
        }
        catch(ParseException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private static void listResources() {
        // list all resources that are on ZooKeeper
        RDZKClient client = new RDZKClientImpl();
        try {
            List<String> resources = client.getResources();
            for (String resource : resources) {
                System.out.println(resource);
            }
        } catch (StepException e) {
            System.err.println("Could not connect to ZooKeeper instance");
        }
    }

    private static void addResource(String resourceName) throws StepException {
        Pattern p = Pattern.compile("^[a-z0-9_-]{3,20}$");
        Matcher m = p.matcher(resourceName);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid resource name, must be alphanumeric / underscores / hyphens" +
                    " and between 3-20 characters");
        }
        RDZKClient client = new RDZKClientImpl();
        if (client.resourceExists(resourceName)) {
            throw new IllegalArgumentException(String.format("Resource '%s' already exists", resourceName));
        }

    }
}
