package com.bluesgao.funnymock.cmd;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CommandParser {
    //默认端口号
    private static int DEFAULT_PORT = 8888;
    //默认数据文件
    private static String DEFAULT_DATA_FILE = "default_data.json";
    //使用说明
    private static final String USAGE = "java -jar /path/funny-monkey.jar -port <port> -file </path/xx.json>";
    //命令集合
    public static Map<String, Object> commandMap = new HashMap<String, Object>(8);

    public static void parse(String[] args) {
        // Create a Parser
        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        options.addOption("h", "help", false, USAGE);
        options.addOption("p", "port", true, "默认端口8888");
        options.addOption("f", "file", true, "JSON数据文件 /usr/local/xx.json");

        // Parse the program arguments
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Set the appropriate variables based on supplied options
        if (commandLine.hasOption('h')) {
            commandMap.put("h", commandLine.getOptionValue("h"));
        }
        if (commandLine.hasOption('p')) {
            commandMap.put("p", commandLine.getOptionValue("p"));
        } else {
            commandMap.put("p", DEFAULT_PORT);
        }
        if (commandLine.hasOption('f')) {
            commandMap.put("f", commandLine.getOptionValue("f"));
        }else{
            commandMap.put("f", DEFAULT_DATA_FILE);
        }
    }
}
