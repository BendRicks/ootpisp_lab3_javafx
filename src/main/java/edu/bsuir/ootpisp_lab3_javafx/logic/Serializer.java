package edu.bsuir.ootpisp_lab3_javafx.logic;

import javafx.collections.ObservableList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Stack;

public class Serializer {

    public static void serializeObjects(File fileToSave, ObservableList list) throws IOException, IllegalAccessException {
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileToSave));
        for (Object obj : list) {
            fileWriter.write(serializeObject(obj));
            fileWriter.write("\n\n");
        }
        fileWriter.close();
    }

    private static int nesting = 0;
    private static final HashSet<Class> serializableClasses = new HashSet<>() {{
        add(byte.class);
        add(Byte.class);
        add(short.class);
        add(Short.class);
        add(int.class);
        add(Integer.class);
        add(long.class);
        add(Long.class);
        add(char.class);
        add(Character.class);
        add(boolean.class);
        add(Boolean.class);
        add(float.class);
        add(Float.class);
        add(double.class);
        add(Double.class);
        add(String.class);
    }};

    public static String serializeObject(Object obj) throws IllegalAccessException {
        nesting++;
        StringBuilder serializedObject = new StringBuilder();
        serializedObject.append(obj.getClass().getSimpleName()).append(" {");
        Stack<Field> fields = getAllFields(obj);
        while (!fields.isEmpty()) {
            Field field = fields.pop();
            field.setAccessible(true);
            serializedObject.append("\n");
            for (int i = 0; i < nesting; i++) {
                serializedObject.append("\t");
            }
            if (serializableClasses.contains(field.getType())) {
                serializedObject.append(field.getName()).append(" = ").append(field.get(obj) != null ? ("\"" + field.get(obj).toString() + "\"") : "null");
            } else {
                if (field.getType().isArray()) {
                    serializedObject.append(field.getName()).append(" = ").append(serializeArray(field.get(obj)));
                } else {
                    serializedObject.append(field.getName()).append(" = ");
                    if (field.get(obj) == null) {
                        serializedObject.append("null");
                    } else {
                        serializedObject.append(serializeObject(field.get(obj)));
                    }
                }
            }
            field.setAccessible(false);
        }
        nesting--;
        serializedObject.append("\n");
        for (int i = 0; i < nesting; i++) {
            serializedObject.append("\t");
        }
        serializedObject.append("}");
        return serializedObject.toString();
    }

    public static String serializeArray(Object obj) throws IllegalAccessException {
        StringBuilder serializedArray = new StringBuilder();
        serializedArray.append("[");
        nesting++;
        Class elemClass = obj.getClass().getComponentType();
        int arrLength = Array.getLength(obj);
        for (int i = 0; i < arrLength; i++) {
            Object arrElement = Array.get(obj, i);
            if (serializableClasses.contains(elemClass)) {
                serializedArray.append("\"").append(arrElement).append("\"");
                if (i < arrLength - 1) {
                    serializedArray.append(",");
                } else {
                    nesting--;
                }
            } else {
                serializedArray.append("\n");
                for (int m = 0; m < nesting; m++) {
                    serializedArray.append("\t");
                }
                if (arrElement != null) {
                    serializedArray.append(serializeObject(arrElement));
                } else {
                    serializedArray.append("null");
                }
                if (i < arrLength - 1) {
                    serializedArray.append(",");
                } else {
                    serializedArray.append("\n");
                    nesting--;
                    for (int m = 0; m < nesting; m++) {
                        serializedArray.append("\t");
                    }
                }
            }
        }
        serializedArray.append("]");
        if (serializedArray.toString().equals("[]")){
            nesting--;
        }
        return serializedArray.toString();
    }

    private static Stack<Field> getAllFields(Object obj) {
        Stack<Field> fields = new Stack<>();
        Class objClass = obj.getClass();
        while (objClass != Object.class) {
            fields.addAll(Arrays.asList(objClass.getDeclaredFields()));
            objClass = objClass.getSuperclass();
        }
        return fields;
    }

}
