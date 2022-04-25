package edu.bsuir.ootpisp_lab3_javafx.entity;

public abstract class ColdWeapons extends HandWeapon{

    private String bladeLength;

    public ColdWeapons() {}

    public ColdWeapons(String name, String bladeLength){
        super(name);
        this.bladeLength = bladeLength;
    }

    public String getBladeLength() {
        return bladeLength;
    }
}
