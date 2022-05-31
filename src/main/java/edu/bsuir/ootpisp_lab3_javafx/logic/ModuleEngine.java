package edu.bsuir.ootpisp_lab3_javafx.logic;

import edu.bsuir.ootpisp_lab3_javafx.entity.Rifle;
import edu.bsuir.ootpisp_lab3_javafx.entity.Saber;
import edu.bsuir.ootpisp_lab3_javafx.entity.Shotgun;
import edu.bsuir.ootpisp_lab3_javafx.entity.Spear;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

public class ModuleEngine {

    private static final Properties properties = new Properties();

    public static void updateModules(ComboBox<Class> comboBox, String modulePath) throws IOException {
        properties.load(ModuleEngine.class.getResourceAsStream("paths.properties"));
        ModuleLoader loader = new ModuleLoader(modulePath, ClassLoader.getSystemClassLoader());
        File dir = new File(modulePath);
        String[] modules = dir.list();
        ObservableList<Class> classesList = FXCollections.observableArrayList(Rifle.class, Shotgun.class, Saber.class, Spear.class);
        if (modules != null) {
            for (String module: modules) {
                try {
                    Class currLoadingClass = loader.loadClass(module);
                    classesList.add(currLoadingClass);
                    if (properties.getProperty(currLoadingClass.getSimpleName()) == null){
                        properties.setProperty(currLoadingClass.getSimpleName(), currLoadingClass.getName());
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        Platform.runLater(() -> comboBox.setItems(classesList));
    }

}
