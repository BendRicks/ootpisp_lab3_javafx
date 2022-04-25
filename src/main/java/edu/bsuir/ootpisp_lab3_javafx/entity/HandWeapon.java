package edu.bsuir.ootpisp_lab3_javafx.entity;

public abstract class HandWeapon {

    private String name;

    public HandWeapon(){}

    public HandWeapon(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
