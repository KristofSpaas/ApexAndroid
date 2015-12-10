package com.groep9.apex.apexandroid.MedischeInfo;


public class CardItemMedischeInfo {

    private String title;
    private String content1, content2, content3, content4;
    private int imageId;

    public CardItemMedischeInfo(String title, String content1, int imageId) {
        this.title = title;
        this.content1 = content1;
        this.imageId = imageId;
    }

    public CardItemMedischeInfo(String title, String content1, String content2, int imageId) {
        this.title = title;
        this.content1 = content1;
        this.content2 = content2;
        this.imageId = imageId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent1() {
        return content1;
    }

    public String getContent2() {
        return content2;
    }

    public String getContent3() {
        return content3;
    }

    public String getContent4() {
        return content4;
    }

    public int getImageId() {
        return imageId;
    }

    public void setContent3(String content3) {
        this.content3 = content3;
    }

    public void setContent4(String content4) {
        this.content4 = content4;
    }
}
