package edu.bsuir.ootpisp_lab3_javafx.logic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Deserializer {

    private static final String FIELD_NAME_REGEXP = "\\w+";
    private static final Pattern fieldNamePattern = Pattern.compile(FIELD_NAME_REGEXP);
    private static final String FIELD_VALUE_REGEXP = "(\"\\w++\")|(null)";

    private static final HashMap<Class, Method> classesParsers = new HashMap() {{
        try {
            put(boolean.class, Boolean.class.getMethod("parseBoolean", String.class));
            put(Boolean.class, Boolean.class.getMethod("parseBoolean", String.class));
            put(float.class, Float.class.getMethod("parseFloat", String.class));
            put(Float.class, Float.class.getMethod("parseFloat", String.class));
            put(double.class, Double.class.getMethod("parseDouble", String.class));
            put(Double.class, Double.class.getMethod("parseDouble", String.class));
            put(long.class, Long.class.getMethod("parseLong", String.class));
            put(Long.class, Long.class.getMethod("parseLong", String.class));
            put(int.class, Integer.class.getMethod("parseInt", String.class));
            put(Integer.class, Integer.class.getMethod("parseInt", String.class));
            put(short.class, Short.class.getMethod("parseShort", String.class));
            put(Short.class, Short.class.getMethod("parseShort", String.class));
            put(byte.class, Byte.class.getMethod("parseByte", String.class));
            put(Byte.class, Byte.class.getMethod("parseByte", String.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }};

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

    private static final Properties properties = new Properties();

    public static ObservableList<Object> deserializeObjects(File fileToOpen) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        properties.load(Serializer.class.getResourceAsStream("paths.properties"));
        Scanner fis = new Scanner(fileToOpen);
        ObservableList<Object> list = FXCollections.observableArrayList();
        ArrayList<String> serializedObject = new ArrayList<>();
        while (fis.hasNextLine()){
            String objPart = fis.nextLine();
            if (objPart.equals("")){
                break;
            }
            serializedObject.add(objPart);
            if (objPart.equals("}")){
                Object obj = deserializeObject(serializedObject);
                if (obj != null) {
                    list.add(obj);
                }
                fis.nextLine();
                serializedObject.clear();
            }
        }
        return list;
    }

    private static Object deserializeObject(ArrayList<String> objectString) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        int objLength = objectString.size() - 1;
        String classString = objectString.get(0);
        Matcher classStringMatcher = fieldNamePattern.matcher(classString);
        classStringMatcher.find();
        classString = classStringMatcher.group();
        String className = properties.getProperty(classString);
        if (className != null) {
            Class objClass = ModuleEngine.getLoader().getLoadedClass(className);
            Object newObject = objClass.getConstructor().newInstance();
            Stack<Class> hierarchy = getClassHierarchy(newObject);
            Class currClass = hierarchy.pop();
            int currClassFieldsAmount = currClass.getDeclaredFields().length - 1;
            int currField = 0;
            for (int i = 1; i < objLength; i++) {
                String paramStr = objectString.get(i);
                String[] paramStrParts = paramStr.split(" = ");
                Matcher fieldNameMatcher = fieldNamePattern.matcher(paramStrParts[0]);
                fieldNameMatcher.find();
                String fieldName = fieldNameMatcher.group();
                StringBuilder stringBuilder = new StringBuilder();
                if (paramStrParts.length > 2) {
                    for (int j = 1; j < paramStrParts.length; j++) {
                        stringBuilder.append(paramStrParts[j]);
                    }
                } else {
                    stringBuilder.append(paramStrParts[1]);
                }
                String fieldValue = stringBuilder.toString();
                Field fieldToSet = currClass.getDeclaredField(fieldName);
                fieldToSet.setAccessible(true);
                if (fieldValue.equals("null")) {
                    fieldToSet.set(newObject, null);
                } else {
                    if (serializableClasses.contains(fieldToSet.getType())) {
                        fieldValue = fieldValue.substring(1, fieldValue.length() - 1);
                        if (fieldToSet.getType() == boolean.class
                                || fieldToSet.getType() == Boolean.class) {
                            fieldToSet.set(newObject, classesParsers.get(fieldToSet.getType()).invoke(null, fieldValue));
                        } else if (fieldToSet.getType() == String.class) {
                            fieldToSet.set(newObject, fieldValue);
                        } else if (fieldToSet.getType() == char.class
                                || fieldToSet.getType() == Character.class) {
                            fieldToSet.set(newObject, fieldValue.charAt(0));
                        } else {
                            fieldToSet.set(newObject, classesParsers.
                                    get(fieldToSet.getType()).invoke(null, fieldValue));
                        }
                    } else {
                        if (fieldToSet.getType().isArray()) {
                            Pattern openBracketPattern = Pattern.compile("\\[");
                            Pattern closeBracketPattern = Pattern.compile("]");
                            ArrayList<String> subObject = new ArrayList<>();
                            int unclosedBlocks = 1;
                            subObject.add(fieldValue);
                            if (!serializableClasses.contains(fieldToSet.getType().getComponentType()) && !fieldValue.equals("[]")) {
                                do {
                                    i++;
                                    String newStr = objectString.get(i);
                                    subObject.add(newStr);
                                    Matcher openBracketMatcher = openBracketPattern.matcher(newStr);
                                    Matcher closeBracketMatcher = closeBracketPattern.matcher(newStr);
                                    while (openBracketMatcher.find()) {
                                        unclosedBlocks++;
                                    }
                                    while (closeBracketMatcher.find()) {
                                        unclosedBlocks--;
                                    }
                                } while (unclosedBlocks != 0);
                            }
                            fieldToSet.set(newObject, deserializeArray(fieldToSet.getType(), subObject));
                        } else {
                            Pattern openBracketPattern = Pattern.compile("[{]");
                            Pattern closeBracketPattern = Pattern.compile("[}]");
                            ArrayList<String> subObject = new ArrayList<>();
                            int unclosedBlocks = 1;
                            subObject.add(fieldValue);
                            do {
                                i++;
                                String newStr = objectString.get(i);
                                subObject.add(newStr);
                                Matcher openBracketMatcher = openBracketPattern.matcher(newStr);
                                Matcher closeBracketMatcher = closeBracketPattern.matcher(newStr);
                                while (openBracketMatcher.find()) {
                                    unclosedBlocks++;
                                }
                                while (closeBracketMatcher.find()) {
                                    unclosedBlocks--;
                                }
                            } while (unclosedBlocks != 0);
                            fieldToSet.set(newObject, deserializeObject(subObject));
                        }
                    }
                }
                fieldToSet.setAccessible(false);
                if (currField >= currClassFieldsAmount && !hierarchy.isEmpty()) {
                    currClass = hierarchy.pop();
                    currField = 0;
                    currClassFieldsAmount = currClass.getDeclaredFields().length - 1;
                } else {
                    currField++;
                }
            }
            return newObject;
        }
        return null;
    }

    private static Object deserializeArray(Class arrClass, ArrayList<String> objectString) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException, NoSuchMethodException, InstantiationException {
        Object resultArr = null;
        Class elemClass = arrClass.getComponentType();
        if (serializableClasses.contains(elemClass)){
            String arrString = objectString.get(0);
            arrString = arrString.substring(2, arrString.length() - 2);
            List<String> params = Arrays.asList(arrString.split("\",\""));
            resultArr = Array.newInstance(elemClass, params.size());
            for (int i = 0; i < params.size(); i++){
                if (elemClass == String.class) {
                    Array.set(resultArr, i, params.get(i));
                } else if (elemClass == char.class
                        || elemClass == Character.class) {
                    Array.set(resultArr, i, params.get(i).charAt(0));
                } else {
                    Array.set(resultArr, i, classesParsers.get(elemClass).invoke(null, params.get(i)));
                }
            }
        } else {
            ArrayList<Object> elements = new ArrayList<>();
            ArrayList<String> subObject = new ArrayList<>();
            for (int i = 1; i < objectString.size()-1; i++) {
                String infoStr = objectString.get(i);
                if (infoStr.contains("null")) {
                    elements.add(null);
                } else {
                    subObject.add(infoStr);
                    Pattern openBracketPattern = Pattern.compile("[{]");
                    Pattern closeBracketPattern = Pattern.compile("[}]");
                    int unclosedBlocks = 1;
                    do {
                        i++;
                        String newStr = objectString.get(i);
                        subObject.add(newStr);
                        Matcher openBracketMatcher = openBracketPattern.matcher(newStr);
                        Matcher closeBracketMatcher = closeBracketPattern.matcher(newStr);
                        while (openBracketMatcher.find()) {
                            unclosedBlocks++;
                        }
                        while (closeBracketMatcher.find()) {
                            unclosedBlocks--;
                        }
                    } while (unclosedBlocks != 0);
                    elements.add(deserializeObject(subObject));
                    subObject.clear();
                }
            }
            resultArr = Array.newInstance(elemClass, elements.size());
            for (int j = 0; j < elements.size(); j++){
                Array.set(resultArr, j, elements.get(j));
            }
        }
        return resultArr;
    }

    private static Stack<Class> getClassHierarchy(Object obj) {
        Stack<Class> hierarchy = new Stack<>();
        Class objClass = obj.getClass();
        while (objClass != Object.class) {
            hierarchy.push(objClass);
            objClass = objClass.getSuperclass();
        }
        return hierarchy;
    }

}
