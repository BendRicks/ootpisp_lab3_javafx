package edu.bsuir.ootpisp_lab3_javafx.logic;

import edu.bsuir.ootpisp_lab3_javafx.entity.Rifle;
import edu.bsuir.ootpisp_lab3_javafx.entity.Saber;
import edu.bsuir.ootpisp_lab3_javafx.entity.Shotgun;
import edu.bsuir.ootpisp_lab3_javafx.entity.Spear;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Properties;

public class ModuleEngine {

    private static final Properties properties = new Properties();
    private static final ModuleLoader loader = new ModuleLoader("plugins", ClassLoader.getSystemClassLoader());

    public static ModuleLoader getLoader(){
        return loader;
    }

    public static void updateModules(ComboBox<Class> comboBox) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, IOException, ClassNotFoundException {
        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
        properties.load(ModuleEngine.class.getResourceAsStream("paths.properties"));
        File dir = new File("plugins");
        String[] modules = dir.list();
        ObservableList<Class> classesList = FXCollections.observableArrayList();
        if (modules != null) {
            for (String module : modules) {
                Class currLoadingClass = null;
                if (!loader.isLoaded(module)) {
                    try {
                        currLoadingClass = loader.loadClass(module);
                        if (!Modifier.isAbstract(currLoadingClass.getModifiers()) && !Modifier.isInterface(currLoadingClass.getModifiers())) {
                            classesList.add(currLoadingClass);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    currLoadingClass = loader.getLoadedClass(module);
                    if (!Modifier.isAbstract(currLoadingClass.getModifiers()) && !Modifier.isInterface(currLoadingClass.getModifiers())) {
                        classesList.add(currLoadingClass);
                    }
                }
                if (properties.getProperty(currLoadingClass.getSimpleName()) == null) {
                    properties.setProperty(currLoadingClass.getSimpleName(), currLoadingClass.getName());
                }
            }
        }
        Platform.runLater(() -> {
            comboBox.setItems(classesList);
            comboBox.getSelectionModel().select(0);
        });
        properties.store(new FileOutputStream("src/main/resources/edu/bsuir/ootpisp_lab3_javafx/logic/paths.properties"), null);
    }

}
