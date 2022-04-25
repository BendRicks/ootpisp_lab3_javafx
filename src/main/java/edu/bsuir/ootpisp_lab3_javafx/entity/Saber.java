package edu.bsuir.ootpisp_lab3_javafx.entity;

public class Saber extends ColdWeapons{

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
