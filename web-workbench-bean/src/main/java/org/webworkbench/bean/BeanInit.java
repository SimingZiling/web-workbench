package org.webworkbench.bean;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bean初始化
 */
public class BeanInit {

    /**
     *
     * @param pack 包名称
     * @return
     */
    public static Set<Class<?>> getClasses(String pack){

        // 创建class集合
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        // 设置是否迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');

        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;

        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(
                    packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    System.err.println("file类型的扫描");
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                }else if("jar".equals(protocol)){
                    System.err.println("jar类型的扫描");
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection())
                                .getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            // 添加到classes
                                            classes.add(Class.forName(packageName + '.' + className));
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
            }catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, Set<Class<?>> classes){
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            // log.warn("用户定义包名 " + packageName + " 下没有任何文件");
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return (recursive && file.isDirectory())
                        || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "."
                                + file.getName(), file.getAbsolutePath(), recursive,
                        classes);
            }else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0,file.getName().length() - 6);
                try {
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean iteration = false;

    public Set<String> scanPackage(String packPath,Set<String> packageSet){
        if(packageSet == null){
            packageSet = new LinkedHashSet<String>();
        }
        // 通过正则获取带星号的包的前缀贺后缀
        Matcher matcher = Pattern.compile("(.*?)\\.\\*(.*)").matcher(packPath);
        if (matcher.matches()) {
            // 当星号后的值为空的时候则进行迭代
            if (matcher.group(2) == null || matcher.group(2).equals("")){
                iteration = true;
            }
            for(String fileName : getPackageFile(matcher.group(1),false)){
                scanPackage(matcher.group(1)+"."+fileName+matcher.group(2),packageSet);
            }
        }else {
            if(getPackageFile(packPath,true).size() !=0){
                packageSet.add(packPath);
            }
        }
        if(iteration){
            for(String fileName : getPackageFile(packPath,false)){
                scanPackage(packPath+"."+fileName,packageSet);
            }
        }
        return packageSet;
    }

    /**
     * 获取包路径中文件
     * @param packPath 包路径
     * @param type 获取类型 true表示文件 false表示文件夹
     * @return 有效文件列表（不包含空目录）
     */
    public Set<String> getPackageFile(String packPath,Boolean type){
        Set<String> packageFile = new LinkedHashSet<String>();

        String packName = packPath;
        String packageDirName = packName.replace('.', '/');
        try {
            Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 迭代包路径
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                if("file".equals(protocol)){
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 获取此包的目录 建立一个File
                    File dir = new File(filePath);
                    // 如果不存在或者文件不是目录
                    if(!dir.exists() || !dir.isDirectory()){
                        break;
                    }
                    // 获取目录中文件列表
                    File[] dirfiles = dir.listFiles();
                    if (dirfiles == null || dirfiles.length <= 0){
                        break;
                    }
                    for (File file : dirfiles){
                        if(type == null){
                            packageFile.add(file.getName());
                        }else if (type){
                            if (!file.isDirectory()) {
                                packageFile.add(file.getName());
                            }
                        }else {
                            if (file.isDirectory()) {
                                packageFile.add(file.getName());
                            }
                        }
                    }
                }else if ("jar".equals(protocol)){
                    // 从此jar包 得到一个枚举类
                    Enumeration<JarEntry> entries = ((JarURLConnection) url.openConnection()).getJarFile().entries();
                    // 同样的进行循环迭代
                    while (entries.hasMoreElements()){
                        // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        // 如果是以/开头的
                        if (name.charAt(0) == '/') {
                            // 获取后面的字符串
                            name = name.substring(1);
                        }
                        if(type == null){

                        }else if(type){
                            if (!entry.isDirectory()){
                                if (name.startsWith(packageDirName)) {
                                    Matcher matcher = Pattern.compile(packageDirName + ".*?/(.*?)").matcher(name);
                                    if (matcher.matches()) {
                                        Matcher fileMatcher = Pattern.compile("/").matcher(matcher.group(1));
                                        if(!fileMatcher.find()){
                                            packageFile.add(matcher.group(1));
                                        }
                                    }
                                }
                            }
                        }else {
                            if (entry.isDirectory()) {
                                // 如果前半部分和定义的包名相同
                                if (name.startsWith(packageDirName)) {
                                    Matcher matcher = Pattern.compile(packageDirName + "/(.*?)/.*").matcher(name);
                                    if (matcher.matches()) {
                                        packageFile.add(matcher.group(1));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return packageFile;
    }

}
