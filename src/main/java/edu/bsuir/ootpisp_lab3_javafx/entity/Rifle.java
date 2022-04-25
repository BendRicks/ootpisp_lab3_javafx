package edu.bsuir.ootpisp_lab3_javafx.entity;

public class Rifle extends Firearms{

    private boolean isBoltAction;

    public Rifle() {}

    public Rifle(String name, String caliber, boolean isBoltAction){
        super(name, caliber);
        this.isBoltAction = isBoltAction;
    }

    public boolean isBoltAction() {
        return isBoltAction;
    }

}
