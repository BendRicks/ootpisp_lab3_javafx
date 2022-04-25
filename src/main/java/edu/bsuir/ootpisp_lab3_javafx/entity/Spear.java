package edu.bsuir.ootpisp_lab3_javafx.entity;

public class Spear extends ColdWeapons{

    private boolean isThrowable;

    public Spear() {}

    public Spear(String name, String bladeLength, boolean isThrowable){
        super(name, bladeLength);
        this.isThrowable = isThrowable;
    }

    public boolean isThrowable() {
        return isThrowable;
    }

}
