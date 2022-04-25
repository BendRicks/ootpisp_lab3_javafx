package edu.bsuir.ootpisp_lab3_javafx;

import edu.bsuir.ootpisp_lab3_javafx.entity.*;
import edu.bsuir.ootpisp_lab3_javafx.logic.Deserializer;
import edu.bsuir.ootpisp_lab3_javafx.logic.Serializer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

public class HelloController {

    @FXML
    private ListView<HandWeapon> objectsListViewer;
    @FXML
    private ComboBox<Class> classesComboBox;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField additParamTextField;
    @FXML
    private RadioButton trueRB;
    @FXML
    private RadioButton falseRB;
    @FXML
    private Label apLabel;
    @FXML
    private Label bpLabel;
    @FXML
    private ToggleGroup booleanGroup;

    private ObservableList<HandWeapon> objectsList;

    @FXML
    public void initialize(){
        objectsList = FXCollections.observableArrayList();
        objectsListViewer.setItems(objectsList);
        ObservableList<Class> classesList =
                FXCollections.observableArrayList(Rifle.class, Shotgun.class, Saber.class, Spear.class);
        classesComboBox.setItems(classesList);
        classesComboBox.setValue(Rifle.class);
    }

    @FXML
    private void editButtonPressed(ActionEvent actionEvent) {
        HandWeapon obj = objectsListViewer.getSelectionModel().getSelectedItem();
        if (obj != null) {
            classesComboBox.setValue(obj.getClass());
            objectsList.remove(obj);
            Stack<Field> fields = getAllFields(obj);
            try {
                Field field = fields.pop();
                field.setAccessible(true);
                String str = field.get(obj).toString();
                field.setAccessible(false);
                nameTextField.setText(str);
                field = fields.pop();
                field.setAccessible(true);
                str = field.get(obj).toString();
                field.setAccessible(false);
                additParamTextField.setText(str);
                field = fields.pop();
                field.setAccessible(true);
                if (field.getBoolean(obj)) {
                    trueRB.setSelected(true);
                } else {
                    falseRB.setSelected(true);
                }
                field.setAccessible(false);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
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

    @FXML
    private void clearButtonPressed(ActionEvent actionEvent) {
        objectsList.clear();
    }

    @FXML
    private void addButtonPressed(ActionEvent actionEvent) {
        Class objClass = (Class) classesComboBox.getSelectionModel().getSelectedItem();
        Class[] params = {String.class, String.class, boolean.class};
        if (objClass != null) {
            try {
                objectsList.add((HandWeapon) objClass.getConstructor(params).newInstance(nameTextField.getText(),
                        additParamTextField.getText(),
                        (trueRB == booleanGroup.getSelectedToggle())));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        nameTextField.setText("");
        additParamTextField.setText("");
        trueRB.setSelected(true);
    }

    @FXML
    private void serializeButtonPressed(ActionEvent actionEvent) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Serialize file");
            File fileToSave = fileChooser.showSaveDialog(nameTextField.getScene().getWindow());
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
            File fileToOpen = fileChooser.showOpenDialog(nameTextField.getScene().getWindow());
            if (fileToOpen != null) {
                objectsList = Deserializer.deserializeObject(fileToOpen);
                objectsListViewer.setItems(objectsList);
            }
        } catch (IOException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteButtonPressed(ActionEvent actionEvent) {
        objectsList.remove(objectsListViewer.getSelectionModel().getSelectedItem());
    }

    private HashMap<Class, String> labels_2 = new HashMap<>() {{
        put(Rifle.class, "Is rifle bolt action?");
        put(Shotgun.class, "Is shotgun pomp?");
        put(Saber.class, "Is blade double-sided?");
        put(Spear.class, "Is spear throwable?");
    }};

    private HashMap<Class, String> labels_1 = new HashMap<>() {{
        put(Rifle.class, "Rifle caliber");
        put(Shotgun.class, "Shotgun caliber");
        put(Saber.class, "Blade length");
        put(Spear.class, "Bayonet length");
    }};

    @FXML
    private void comboBoxOnHidden(){
        Class obj = classesComboBox.getSelectionModel().getSelectedItem();
        apLabel.setText(labels_1.get(obj));
        bpLabel.setText(labels_2.get(obj));
    }
}