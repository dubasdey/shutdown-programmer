package com.github.dubasdey;

public class ActionModel {

    private String label;

    private String action;

    public ActionModel(String label, String action){
        this.label = label;
        this.action = action;
    }

    public String getLabel() {
        return label;
    }

    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return label;
    }
}
