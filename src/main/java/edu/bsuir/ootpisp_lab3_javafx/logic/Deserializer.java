package edu.bsuir.ootpisp_lab3_javafx.logic;

import edu.bsuir.ootpisp_lab3_javafx.entity.HandWeapon;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Deserializer {

    private static final String FIELD_NAME_REGEXP = "\\w+?=";

    public static ObservableList<HandWeapon> deserializeObject(File fileToOpen) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        HashMap<Class, Method> parsers = new HashMap<>(){{
            put(int.class, Integer.class.getDeclaredMethod("parseInt", String.class));
            put(Integer.class, Integer.class.getDeclaredMethod("parseInt", String.class));
            put(float.class, Float.class.getDeclaredMethod("parseFloat", String.class));
            put(Float.class, Float.class.getDeclaredMethod("parseFloat", String.class));
            put(double.class, Double.class.getDeclaredMethod("parseDouble", String.class));
            put(Double.class, Double.class.getDeclaredMethod("parseDouble", String.class));
            put(boolean.class, Boolean.class.getDeclaredMethod("parseBoolean", String.class));
            put(Boolean.class, Boolean.class.getDeclaredMethod("parseBoolean", String.class));
        }};
        HashMap<Class, Class> parserTypes = new HashMap<>(){{
            put(int.class, Integer.class);
            put(Integer.class, Integer.class);
            put(float.class, Float.class);
            put(Float.class, Float.class);
            put(double.class, Double.class);
            put(Double.class, Double.class);
            put(boolean.class, Boolean.class);
            put(Boolean.class, Boolean.class);
        }};
        Properties properties = new Properties();
        properties.load(Serializer.class.getResourceAsStream("paths.properties"));

        ObservableList<HandWeapon> list = FXCollections.observableArrayList();
        Scanner fis = new Scanner(fileToOpen);
        while (fis.hasNextLine()) {
            String classStr = fis.nextLine();
            if (fis.nextLine().equals("{")) {
                Pattern fieldNamePattern = Pattern.compile(FIELD_NAME_REGEXP);
                classStr = (String) properties.get(classStr);
                Class objClass = Class.forName(classStr);
                Object newObject = objClass.getConstructor().newInstance();
                Stack<Field> fields = getAllFields(newObject);
                while (!fields.isEmpty()) {
                    Field field = fields.pop();
                    String str = fis.nextLine();
                    Matcher matcher = fieldNamePattern.matcher(str);
                    matcher.find();
                    str = str.substring(matcher.end());
                    field.setAccessible(true);
                    if (field.getType() != String.class) {
                        Method method = parsers.get(field.getType());
                        method.setAccessible(true);
                        field.set(newObject, method.invoke(parserTypes.get(field.getType()), str));
                        method.setAccessible(false);
                    } else {
                        field.set(newObject, str);
                    }
                    field.setAccessible(false);
                }
                if (!fis.nextLine().equals("}")){
                    throw new IOException();
                }
                list.add((HandWeapon) newObject);
            } else {
                throw new IOException();
            }
        }
        fis.close();
        return list;
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
