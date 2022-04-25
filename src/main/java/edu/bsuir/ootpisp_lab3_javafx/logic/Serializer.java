package edu.bsuir.ootpisp_lab3_javafx.logic;

import javafx.collections.ObservableList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Stack;

public class Serializer {

    public static void serializeObjects(File fileToSave, ObservableList list) throws IOException, IllegalAccessException {
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileToSave));
        for (Object obj : list) {
            fileWriter.write(obj.getClass().getSimpleName() + "\n{");
            Stack<Field> fields = getAllFields(obj);
            while (!fields.isEmpty()) {
                Field field = fields.pop();
                field.setAccessible(true);
                fileWriter.write("\n" + field.getName()
                        + "=" + field.get(obj).toString());
                field.setAccessible(false);
            }
            fileWriter.write("\n}\n");
        }
        fileWriter.close();
    }

    private static Stack<Field> getAllFields(Object obj){
        Stack<Field> fields = new Stack<>();
        Class objClass = obj.getClass();
        while (objClass != Object.class) {
            fields.addAll(Arrays.asList(objClass.getDeclaredFields()));
            objClass = objClass.getSuperclass();
        }
        return fields;
    }

}
