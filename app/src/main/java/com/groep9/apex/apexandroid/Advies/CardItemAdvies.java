package com.groep9.apex.apexandroid.Advies;

public class CardItemAdvies {

    private long id;
    private String title;
    private String content;
    private int imageId;

    public CardItemAdvies(long id, String title, String content, int imageId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageId = imageId;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getImageId() {
        return imageId;
    }

}
