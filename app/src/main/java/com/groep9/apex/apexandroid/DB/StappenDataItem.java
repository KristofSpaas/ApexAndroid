package com.groep9.apex.apexandroid.DB;


public class StappenDataItem {
    private long id;
    private long stappen;
    private long dateInMiliis;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStappen() {
        return stappen;
    }

    public void setStappen(long stappen) {
        this.stappen = stappen;
    }

    public long getDateInMiliis() {
        return dateInMiliis;
    }

    public void setDateInMiliis(long dateInMiliis) {
        this.dateInMiliis = dateInMiliis;
    }

}
