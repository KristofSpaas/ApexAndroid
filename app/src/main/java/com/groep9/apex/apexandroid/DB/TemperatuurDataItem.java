package com.groep9.apex.apexandroid.DB;


public class TemperatuurDataItem {

    private long id;
    private float temperatuur;
    private long dateInMiliis;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public float getTemperatuur() {
        return temperatuur;
    }

    public void setTemperatuur(float temperatuur) {
        this.temperatuur = temperatuur;
    }

    public long getDateInMiliis() {
        return dateInMiliis;
    }

    public void setDateInMiliis(long dateInMiliis) {
        this.dateInMiliis = dateInMiliis;
    }
}
