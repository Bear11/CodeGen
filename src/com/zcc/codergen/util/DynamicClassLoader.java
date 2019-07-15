package com.zcc.codergen.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author zilianghuang
 * @module DynamicClassLoader
 * @date 2019/7/10 15:23
 * @desc TODO
 */

public class DynamicClassLoader extends ClassLoader {
    private static String basePath = "src/main/java";

    /**
     * @param className 全限定名！！
     * @return
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("deprecation")
    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        String clazzPath = basePath + File.separator + className.replace(".", File.separator) + ".class";
        File classFile = new File(clazzPath);
        if (!classFile.exists()) {
            throw new ClassNotFoundException(classFile.getPath() + " 不存在");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteBuffer bf = ByteBuffer.allocate(1024);
        FileInputStream fis = null;
        FileChannel fc = null;
        Path path = Paths.get(clazzPath);
        byte[] bytes = new byte[0];
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            fis = new FileInputStream(classFile);
//            fc = fis.getChannel();
//            while (fc.read(bf) > 0) {
//                bf.flip();
//                bos.write(bf.array(), 0, bf.limit());
//                bf.clear();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                assert fis != null;
//                fis.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                assert fc != null;
//                fc.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        return defineClass(className, bytes, 0, bytes.length);
    }

}