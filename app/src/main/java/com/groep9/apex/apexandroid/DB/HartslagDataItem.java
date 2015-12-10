package com.groep9.apex.apexandroid.DB;


public class HartslagDataItem {

    private long id;
    private int hartslag;
    private long dateInMiliis;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getHartslag() {
        return hartslag;
    }

    public void setHartslag(int hartslag) {
        this.hartslag = hartslag;
    }

    public long getDateInMiliis() {
        return dateInMiliis;
    }

    public void setDateInMiliis(long dateInMiliis) {
        this.dateInMiliis = dateInMiliis;
    }
}
