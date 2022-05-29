package edu.bsuir.ootpisp_lab3_javafx.entity;

import java.io.Serializable;

public class Rifle extends Firearms implements Serializable {

    private boolean isBoltAction;
    private RifleOptions options;

    public Rifle() {}

    public Rifle(String name, String caliber, boolean isBoltAction){
        super(name, caliber);
        this.isBoltAction = isBoltAction;
    }

    public boolean isBoltAction() {
        return isBoltAction;
    }

}
