package edu.bsuir.ootpisp_lab3_javafx.entity;

public class Shotgun extends Firearms{

    private boolean isPomp;

    public Shotgun() {}

    public Shotgun(String name, String caliber, boolean isPomp){
        super(name, caliber);
        this.isPomp = isPomp;
    }

    public boolean isPomp() {
        return isPomp;
    }

}
