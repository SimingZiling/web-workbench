package org.webworkbench.example.webapp;

import org.webworkbench.bean.BeanInit;

import java.io.File;

public class WebApp {

    public static void main(String[] args) {
//        System.out.println(BeanInit.getClasses("org.webworkbench.example.controller"));
//        BeanInit.scanPackage("org.webworkbench.*.controller.*.model");
//        System.out.println(new BeanInit().scanPackage("org.webworkbench.*.controller.*",null));
//        System.out.println(new BeanInit().scanPackage("org.webworkbench.example.controller",null));
        for (String as : new BeanInit().scanPackage("org.webworkbench.example.controller.*",null)) {
            for (String a : new BeanInit().getPackageFile(as, true)) {
                System.out.println(as+"."+a);
            }
        }
    }

}
