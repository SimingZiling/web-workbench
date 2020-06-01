package org.webworkbench.example.webapp;

import org.webworkbench.core.configuration.SystemConfiguration;


public class WebApp {

    public static void main(String[] args) {
        SystemConfiguration.systemConfiguration.put("S","22");
        System.out.println(SystemConfiguration.systemConfiguration.get("S"));
        SystemConfiguration.systemConfiguration.put("Ss","2222131");
        System.out.println(SystemConfiguration.systemConfiguration.get("Ss"));
    }

}
