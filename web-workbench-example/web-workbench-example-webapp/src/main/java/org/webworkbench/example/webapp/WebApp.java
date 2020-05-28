package org.webworkbench.example.webapp;

import org.webworkbench.bean.BeanInit;

public class WebApp {

    public static void main(String[] args) {
        System.out.println(BeanInit.getClasses("org.webworkbench.example.controller"));
    }

}
