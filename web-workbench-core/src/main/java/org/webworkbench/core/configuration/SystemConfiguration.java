package org.webworkbench.core.configuration;

import java.util.HashMap;

/**
 * 系统配置
 */
public class SystemConfiguration extends HashMap {

    public static SystemConfiguration systemConfiguration;

    static {
        systemConfiguration = new SystemConfiguration();
    }

}
