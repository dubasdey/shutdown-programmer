
package com.github.dubasdey;

public class TimeModel {

    private String label;

    private int minutes;

    public TimeModel(String label, int minutes){
        this.label = label;
        this.minutes = minutes;
    }

    public String getLabel() {
        return label;
    }

    public int getMinutes() {
        return minutes;
    }

    @Override
    public String toString() {
        return label;
    }
}
