package edu.bsuir.ootpisp_lab3_javafx.entity;

public abstract class Firearms extends HandWeapon{

    private String caliber;

    public Firearms(){}

    public Firearms(String name, String caliber){
        super(name);
        this.caliber = caliber;
    }

    public String getCaliber() {
        return caliber;
    }
}
