package edu.bsuir.ootpisp_lab3_javafx.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ModuleLoader extends ClassLoader {

    private String pathtobin;

    public ModuleLoader(String pathtobin, ClassLoader parent) {
        super(parent);
        this.pathtobin = pathtobin;
    }

    @Override
    public Class<?> findClass(String className) throws ClassNotFoundException {
        try {
            className = className.replaceAll(".class", "");
            byte[] b = fetchClassFromFS(pathtobin + "\\" + className + ".class");
            return defineClass(className, b, 0, b.length);
        } catch (IOException ex) {
            return super.findClass(className);
        }

    }

    private byte[] fetchClassFromFS(String path) throws IOException {
        InputStream is = new FileInputStream(path);
        long length = new File(path).length();
        if (length > Integer.MAX_VALUE) {
            throw new IOException("File is too large");
        }
        byte[] bytes = new byte[(int)length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+path);
        }
        is.close();
        return bytes;
    }

    public boolean isLoaded(String classPath) {
        File file = new File(classPath);
        String[] nameParts = file.getName().split("\\.");
        StringBuilder stringBuilder = new StringBuilder();
        if (nameParts.length > 2){
            for (int i = 0; i < nameParts.length - 1; i++){
                stringBuilder.append(nameParts[i]);
                if (i < nameParts.length - 2){
                    stringBuilder.append(".");
                }
            }
        } else {
            stringBuilder.append(nameParts[0]);
        }
        String className = stringBuilder.toString();
        return findLoadedClass(className) != null;
    }

    public Class getLoadedClass(String classPath){
        File file = new File(classPath);
        String[] nameParts = file.getName().split("\\.");
        StringBuilder stringBuilder = new StringBuilder();
        if (nameParts.length > 2){
            for (int i = 0; i < nameParts.length - 1; i++){
                stringBuilder.append(nameParts[i]);
                if (i < nameParts.length - 2){
                    stringBuilder.append(".");
                }
            }
        } else {
            stringBuilder.append(nameParts[0]);
        }
        String className = stringBuilder.toString();
        return findLoadedClass(className);
    }
}