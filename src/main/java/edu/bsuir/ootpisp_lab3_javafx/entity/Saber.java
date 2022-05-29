package edu.bsuir.ootpisp_lab3_javafx.entity;

import java.io.Serializable;

public class Saber extends ColdWeapons implements Serializable {

    private boolean isDoubleSided;

    public Saber() {}

    public Saber(String name, String bladeLength, boolean isDoubleSided){
        super(name, bladeLength);
        this.isDoubleSided = isDoubleSided;
    }

    public boolean isDoubleSided() {
        return isDoubleSided;
    }
}
