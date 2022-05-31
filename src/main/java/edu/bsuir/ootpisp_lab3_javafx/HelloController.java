package edu.bsuir.ootpisp_lab3_javafx;

import edu.bsuir.ootpisp_lab3_javafx.entity.*;
import edu.bsuir.ootpisp_lab3_javafx.logic.Deserializer;
import edu.bsuir.ootpisp_lab3_javafx.logic.ModuleEngine;
import edu.bsuir.ootpisp_lab3_javafx.logic.Serializer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.regex.Pattern;

public class HelloController {

    @FXML
    private ListView<Object> objectsListViewer;
    @FXML
    private ComboBox<Class> classesComboBox;
    private TextField additParamTextField;
    private RadioButton trueRB;
    private RadioButton falseRB;
    private Label apLabel;
    private TextArea viewTextArea;

    private ObservableList<Object> objectsList;

    private final String INT_REGEXP = "\\d*";
    private final String FLOAT_REGEXP = "([0-9]+(.)?[0-9]*)?";
    private final String CHAR_REGEXP = "[^#?&$]?";
    private final String STRING_REGEXP = "[^#?&$]*";
    private Pattern regexPattern = Pattern.compile(INT_REGEXP);
    private Stage addStage;
    private Stage viewStage;
    private Button nextBtn;

    @FXML
    public void initialize() {
        objectsList = FXCollections.observableArrayList();
        objectsListViewer.setItems(objectsList);
        ObservableList<Class> classesList =
                FXCollections.observableArrayList(Rifle.class, Shotgun.class, Saber.class, Spear.class);
        classesComboBox.setItems(classesList);
        classesComboBox.setValue(Rifle.class);
        buffFieldCounters = new Stack<>();
        buffObjects = new Stack<>();
        buffFields = new Stack<>();
        try {
            FXMLLoader addStagefxmlLoader = new FXMLLoader(HelloController.class.getResource("add-window.fxml"));
            Scene addScene = new Scene(addStagefxmlLoader.load(), 280, 110);
            addStage = new Stage();
            addStage.setTitle("Serializer");
            addStage.setResizable(false);
            addStage.setScene(addScene);
            additParamTextField = (TextField) addStagefxmlLoader.getNamespace().get("additParamTextField");
            trueRB = (RadioButton) addStagefxmlLoader.getNamespace().get("trueRB");
            falseRB = (RadioButton) addStagefxmlLoader.getNamespace().get("falseRB");
            apLabel = (Label) addStagefxmlLoader.getNamespace().get("apLabel");
            nextBtn = (Button) addStagefxmlLoader.getNamespace().get("nextBtn");

            nextBtn.setOnAction(actionEvent -> {
                boolean isEnded = false;
                try {
                    fields.get(currFieldCount).setAccessible(true);
                    if (fields.get(currFieldCount).getType() == boolean.class
                            || fields.get(currFieldCount).getType() == Boolean.class) {
                        fields.get(currFieldCount).set(currObject, trueRB.isSelected());
                    } else if (fields.get(currFieldCount).getType() == String.class) {
                        fields.get(currFieldCount)
                                .set(currObject, additParamTextField.getText());
                    } else if (fields.get(currFieldCount).getType() == char.class
                            || fields.get(currFieldCount).getType() == Character.class) {
                        fields.get(currFieldCount)
                                .set(currObject, additParamTextField.getText().charAt(0));
                    } else {
                        fields.get(currFieldCount)
                                .set(currObject,
                                        classesParsers.get(fields.get(currFieldCount).getType())
                                                .invoke(null, additParamTextField.getText()));
                    }
                    fields.get(currFieldCount).setAccessible(false);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    makeNotification(Alert.AlertType.ERROR, "Error Dialog", "Error", "An error has occurred while parsing parameters \n" + e.getMessage());
                }
                currFieldCount++;
                try {
                    while (currFieldCount >= fields.size()) {
                        if (!buffObjects.isEmpty() && !buffFields.isEmpty() && !buffFieldCounters.isEmpty()) {
                            currObject = buffObjects.pop();
                            fields = buffFields.pop();
                            currFieldCount = buffFieldCounters.pop();
                            currFieldCount++;
                        } else {
                            addStage.hide();
                            isEnded = true;
                            break;
                        }
                    }
                    if (!isEnded) {
                        while (!serializableClasses.contains(fields.get(currFieldCount).getType())) {
                            Field currField = fields.get(currFieldCount);
                            currField.setAccessible(true);
                            if (currField.get(currObject) == null) {
                                Object newObj = currField.getType().getConstructor().newInstance();
                                currField.set(currObject, newObj);
                                if (!serializableClasses.contains(currField.getType()) && !(currField.get(currObject) instanceof Serializable)){
                                    throw new Exception("There's not serializable class");
                                }
                                buffFields.push(fields);
                                buffObjects.push(currObject);
                                buffFieldCounters.push(currFieldCount);
                                currObject = newObj;
                            } else {
                                buffFields.push(fields);
                                buffObjects.push(currObject);
                                buffFieldCounters.push(currFieldCount);
                                currObject = currField.get(currObject);
                            }
                            currField.setAccessible(false);
                            currFieldCount = 0;
                            fields = getAllFields(currObject);
                        }
                        updateAddInfo(fields.get(currFieldCount));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            additParamTextField.setVisible(false);
            trueRB.setVisible(false);
            falseRB.setVisible(false);
            TextFormatter<?> formatter = new TextFormatter<>(change -> {
                if (regexPattern.matcher(change.getControlNewText()).matches()) {
                    return change; // allow this change to happen
                } else {
                    return null; // prevent change
                }
            });
            additParamTextField.setTextFormatter(formatter);

            FXMLLoader objViewFxmlLoader = new FXMLLoader(HelloController.class.getResource("object-view.fxml"));
            Scene objViewScene = new Scene(objViewFxmlLoader.load(), 300, 400);
            viewStage = new Stage();
            viewStage.setTitle("Object viewer");
            viewStage.setResizable(false);
            viewStage.setScene(objViewScene);
            viewTextArea = (TextArea) objViewFxmlLoader.getNamespace().get("viewTextArea");
            viewTextArea.setEditable(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void makeNotification(Alert.AlertType type, String title, String headerText, String contentText) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }

    @FXML
    private void editButtonPressed(ActionEvent actionEvent) {
        currObject = objectsListViewer.getSelectionModel().getSelectedItem();
        if (currObject != null) {
            fields = getAllFields(currObject);
            addStage.show();
            currFieldCount = 0;
            updateAddInfo(fields.get(currFieldCount));
        }
    }

    @FXML
    private void updateButtonPressed(){
        try {
            ModuleEngine.updateModules(classesComboBox, "C:\\Users\\bendr\\Documents\\serialize_plugins");
        } catch (IOException e) {
            makeNotification(Alert.AlertType.ERROR, "Error", "Loading error", "Ocuured an error while loading classes");
        }
    }

    private static ArrayList<Field> getAllFields(Object obj) {
        ArrayList<Field> fields = new ArrayList<>();
        Stack<Class> classesIerarchy = new Stack<>();
        Class objClass = obj.getClass();
        while (objClass != Object.class) {
            classesIerarchy.push(objClass);
            objClass = objClass.getSuperclass();
        }
        while (!classesIerarchy.isEmpty()) {
            fields.addAll(Arrays.asList(classesIerarchy.pop().getDeclaredFields()));
        }
        return fields;
    }

    @FXML
    private void clearButtonPressed(ActionEvent actionEvent) {
        objectsList.clear();
    }

    private ArrayList<Field> fields;
    private Stack<ArrayList<Field>> buffFields;
    private int currFieldCount;
    private Stack<Integer> buffFieldCounters;
    private Object currObject;
    private Stack<Object> buffObjects;

    private void updateAddInfo(Field field) {
        try {
            field.setAccessible(true);
            if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                additParamTextField.setVisible(false);
                falseRB.setSelected(true);
                trueRB.setSelected((Boolean) field.get(currObject));
                trueRB.setVisible(true);
                falseRB.setVisible(true);
                apLabel.setText(field.getName());
            } else {
                additParamTextField.setVisible(true);
                trueRB.setVisible(false);
                falseRB.setVisible(false);
                additParamTextField.setText(String.valueOf(field.get(currObject)));
                apLabel.setText(field.getName());
                regexPattern = Pattern.compile(classesRegex.get(field.getType()));
            }
            apLabel.setText(field.getType().getSimpleName() + " " + field.getName());
            field.setAccessible(false);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void addButtonPressed(ActionEvent actionEvent) {
        Class objClass = classesComboBox.getSelectionModel().getSelectedItem();
        if (objClass != null) {
            try {
                currObject = objClass.getConstructor().newInstance();
                objectsList.add(currObject);
                fields = getAllFields(currObject);
                addStage.show();
                currFieldCount = 0;
                updateAddInfo(fields.get(currFieldCount));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void serializeButtonPressed(ActionEvent actionEvent) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Serialize file");
            File fileToSave = fileChooser.showSaveDialog(additParamTextField.getScene().getWindow());
            if (fileToSave != null) {
                Serializer.serializeObjects(fileToSave, objectsList);
            }
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deserializeButtonPressed(ActionEvent actionEvent) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open serialized file");
            File fileToOpen = fileChooser.showOpenDialog(additParamTextField.getScene().getWindow());
            if (fileToOpen != null) {
                objectsList = Deserializer.deserializeObjects(fileToOpen);
                objectsListViewer.setItems(objectsList);
            }
        } catch (IOException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 InstantiationException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void viewButtonPressed(ActionEvent actionEvent){
        Object obj = objectsListViewer.getSelectionModel().getSelectedItem();
        if (obj != null){
            try {
                viewTextArea.clear();
                viewTextArea.setText(Serializer.serializeObject(obj));
                viewStage.show();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    private void deleteButtonPressed(ActionEvent actionEvent) {
        objectsList.remove(objectsListViewer.getSelectionModel().getSelectedItem());
    }

    HashSet<Class> serializableClasses = new HashSet<>() {{
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

    HashMap<Class, String> classesRegex = new HashMap() {{
        put(char.class, CHAR_REGEXP);
        put(Character.class, CHAR_REGEXP);
        put(String.class, STRING_REGEXP);
        put(float.class, FLOAT_REGEXP);
        put(Float.class, FLOAT_REGEXP);
        put(double.class, FLOAT_REGEXP);
        put(Double.class, FLOAT_REGEXP);
        put(long.class, INT_REGEXP);
        put(Long.class, INT_REGEXP);
        put(int.class, INT_REGEXP);
        put(Integer.class, INT_REGEXP);
        put(short.class, INT_REGEXP);
        put(Short.class, INT_REGEXP);
        put(byte.class, INT_REGEXP);
        put(Byte.class, INT_REGEXP);
    }};

    HashMap<Class, Method> classesParsers = new HashMap() {{
        try {
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
}