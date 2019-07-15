package com.zcc.codergen.util;

import com.ctriposs.baiji.rpc.common.apidoc.DtoDoc;
import com.ctriposs.baiji.rpc.common.apidoc.FieldDoc;
import com.sun.codemodel.*;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author zilianghuang
 * @module AutoGenerator
 * @date 2019/7/9 15:14
 * @desc 制定包名 类名 目标类名 生成目标类名的包装类
 */
public class AutoGenerator {
    private static String basePath = "src\\main\\java";
    private static String packagePath = "com.ctrip.ibu.hotel.controller.core.dto.input.orderbooking";
    private static String prefix;
    private static String suffix;
    private static List<String> passStrings = new ArrayList<>(Arrays.asList("Schema", "head", "serialVersionUID", "ResponseStatusType", "responseStatus", ""));
    private static HashMap<String, String> comments = null;
    static String targetRoot = System.getProperty("user.dir") + "\\target\\classes\\";

    static {
        comments = new HashMap<>();
        comments.put("author", "");
        comments.put("desc", "");
    }

    public static void setAuthor(String author) {
        comments.replace("author", author);
    }

    public static void setDesc(String desc) {
        comments.replace("desc", desc);
    }

//    @Test
//    public void test1() {
//        String className = "CreateOrderRequestDto";
//        String pkg = "com.ctrip.ibu.hotel.controller.core.dto.input.orderbooking.createorder";
//        generator(pkg, className, ReservationRequest.class);
//    }

    public static void generator(String pkg, String target, String prefix, String suffix, Class<?> src) {
        packagePath = pkg;
        generate(target, src);

        String dirPath = basePath + File.separator + packagePath.replace(".", File.separator);
        File dir = new File(dirPath);
        if (dir.isDirectory()) {
            String[] list = dir.list();
            assert list != null;
            for (int i = 0; i < list.length; i++) {
                if (list[i].endsWith(".class")) {
                    File targetFile = new File(dirPath + File.separator + list[i]);
                    targetFile.delete();
                }
            }
        }
        File targetDir = new File(targetRoot);
        if (targetDir.exists()) {
            System.out.println(deleteDir(targetDir) ? "删除classes文件夹OK" : "没删到Class文件,手动删除target文件夹");
        }
        System.out.println("搞定");
    }

    /**
     * @param className 新生成的类名
     * @param clazz     目标类
     * @return 全限定类名
     */
    private static String generate(String className, Class<?> clazz) {
        try {
            String s = packagePath + "." + className;
            Class.forName(s);
            return s;
        } catch (ClassNotFoundException e) {
            System.out.println("没有");
        }
        JCodeModel jCodeModel = new JCodeModel();
        try {
            JPackage jPackage = jCodeModel._package(packagePath);
            JDefinedClass jDefinedClass = null;
            try {
                jDefinedClass = jPackage._class(className);
            } catch (JClassAlreadyExistsException e) {
                jDefinedClass = e.getExistingClass();
            }

            JDocComment classDoc = jDefinedClass.javadoc();

            //添加类注释
            DtoDoc dtoDoc = clazz.getAnnotation(DtoDoc.class);
            classDoc.addXdoclet("author " + comments.get("author"));
            classDoc.addXdoclet("module " + className);
            classDoc.addXdoclet("date " + LocalDate.now());
            classDoc.addXdoclet("desc " + (dtoDoc == null ? "todo" : dtoDoc.value()));

            List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
            for (Field field : fields) {
                // 类
                Class<?> type = field.getType();
                // 类名简写
                String typeSimpleName = type.getSimpleName();
                // 字段名
                String fieldName = field.getName();

                // 忽略的字段
                if (passStrings.contains(fieldName) || passStrings.contains(typeSimpleName)) {
                    continue;
                }

                Type genericType = field.getGenericType();

                if (isJavaClass(type) && !(genericType instanceof ParameterizedType)) {
                    JType jType = jDefinedClass.owner()._ref(type);
                    generateFieldByJTypeWithComment(jDefinedClass, JMod.PRIVATE, jType, field);
                    continue;
                }

                if (!isJavaClass(type) && !(genericType instanceof ParameterizedType)) {
                    String newClassName = prefix + type.getSimpleName() + suffix;
                    Class<?> generate = null;

                    try {
                        generate = Class.forName(packagePath + "." + newClassName);
                        System.out.println("搞到了" + newClassName);
                    } catch (ClassNotFoundException e) {
                        String newClzString = generate(newClassName, type);
//                    try {
//                        generate = Class.forName(newClzString);
//                    } catch (ClassNotFoundException e) {
                        generate = loadClazz(newClzString);//
                    }

//                    }
//                    jDefinedClass.field(JMod.PRIVATE, generate, fieldName).javadoc();
                    generateFieldWithComment(jDefinedClass, JMod.PRIVATE, generate, field);
                    continue;
                }
                if ("List".equals(typeSimpleName)) {
                    Class<?> name = null;
                    try {
                        name = Class.forName((((ParameterizedType) genericType).getActualTypeArguments()[0]).getTypeName());
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (!isJavaClass(name)) {
                        String newClassName = prefix + type.getSimpleName() + suffix;
                        String newClzString = generate(newClassName, name);
                        try {
                            name = Class.forName(newClzString);
                        } catch (ClassNotFoundException e) {
                            name = loadClazz(newClzString);
                        }
                    }
                    JClass arrayListClass = jCodeModel.ref(List.class);
                    JClass narrow = arrayListClass.narrow(name);
//                    jDefinedClass.field(JMod.PRIVATE, narrow, fieldName);
                    generateFieldByJTypeWithComment(jDefinedClass, JMod.PRIVATE, narrow, field);
                }

            }
            jCodeModel.build(new File(basePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return packagePath + "." + className;
    }


    public static Class<?> loadClazz(String newClzString) {
        FileReader fr = null;
        BufferedReader bfr = null;
        String javaCodePath = (basePath + File.separator + newClzString.replace(".", File.separator) + ".java");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(javaCodePath);
        JavaCompiler.CompilationTask cTask = compiler.getTask(null, fileManager, null, null, null, fileObjects);
        cTask.call();
        try {
            fileManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        int run = compiler.run(null, null, null, javaCodePath);
        Class<?> aClass = null;
        DynamicClassLoader dynamicClassLoader = new DynamicClassLoader();
        String replace = javaCodePath.replace(".java", ".class");
        File clazzFile = new File(System.getProperty("user.dir") + File.separator + replace);
        try {
            FileUtils.copyFile(clazzFile, new File(targetRoot + newClzString.replace(".", File.separator) + ".class"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                aClass = Class.forName(newClzString);
                break;
            } catch (ClassNotFoundException e) {
                compiler.run(null, null, null, javaCodePath);
                try {
                    aClass = dynamicClassLoader.findClass(newClzString);
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
                break;
            }
        }
        return aClass;
    }

    private static boolean generateFieldWithComment(JDefinedClass jDefinedClass, int mods, Class<?> type, Field field) {
        List<String> comments = new ArrayList<>();
        FieldDoc property = field.getAnnotation(FieldDoc.class);
        comments.add(property == null ? null : property.value());

        JFieldVar jFieldVar = null;
        String fieldName = field.getName();
        try {
            jFieldVar = jDefinedClass.field(mods, type, fieldName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (jFieldVar == null) {
            System.out.println("为啥");
            return false;
        }
        JDocComment javadoc = jFieldVar.javadoc();
        for (String s : comments) {
            boolean add = javadoc.add(s);
            if (!add) {
                return false;
            }
        }
        JMethod defineGetter = jDefinedClass.method(JMod.PUBLIC, type, "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
        JBlock getterBody = defineGetter.body();
        getterBody._return(JExpr.ref(fieldName));

        JMethod defineSetter = jDefinedClass.method(JMod.PUBLIC, void.class, "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
        defineSetter.param(type, fieldName);
        JFieldRef thisField = JExpr._this().ref(fieldName);
        defineSetter.body().assign(thisField, JExpr.ref(fieldName));
        return true;
    }

    /**
     * 生成字段、字段注释、字段的getter、setter方法
     *
     * @param jDefinedClass
     * @param mods
     * @param type
     * @param field
     * @return
     */

    public static boolean generateFieldByJTypeWithComment(JDefinedClass jDefinedClass, int mods, JType type, Field field) {
        List<String> comments = new ArrayList<>();
        FieldDoc property = field.getAnnotation(FieldDoc.class);
        comments.add(property == null ? null : property.value());

        String fieldName = field.getName();
        JDocComment javadoc = jDefinedClass.field(mods, type, fieldName).javadoc();
        for (String s : comments) {
            boolean add = javadoc.add(s);
            if (!add) {
                return false;
            }
        }
        JMethod defineGetter = jDefinedClass.method(JMod.PUBLIC, type, "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
        JBlock getterBody = defineGetter.body();
        getterBody._return(JExpr.ref(fieldName));

        JMethod defineSetter = jDefinedClass.method(JMod.PUBLIC, void.class, "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
        defineSetter.param(type, fieldName);
        JFieldRef thisField = JExpr._this().ref(fieldName);
        defineSetter.body().assign(thisField, JExpr.ref(fieldName));
        return true;
    }

    /**
     * 判断是否java原有类型 原理是Java原有类型是bootstrap加载
     *
     * @param clz
     * @return
     */
    private static boolean isJavaClass(Class<?> clz) {
        return clz != null && clz.getClassLoader() == null;
    }

    /**
     * 递归删除文件夹,只有文件夹为空才能被删除
     *
     * @param dir
     * @return
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

}
