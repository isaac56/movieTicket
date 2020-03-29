package com.tistory.webnautes.mymovie;

import android.graphics.Bitmap;

/**
 * Created by isaac on 2018-10-14.
 */

public class PersonInformation {
    private Bitmap image;
    private String kName;
    private String eName;
    private String part;

    public PersonInformation (Bitmap image, String kName, String eName, String part) {
        this.image = image;
        this.kName = kName;
        this.eName = eName;
        this.part = part;
    }

    public Bitmap getImage() {return this.image;}
    public String getKName() {return this.kName;}
    public String getEName() {return this.eName;}
    public String getPart() {return this.part;}

    public void setImage(Bitmap image) {this.image = image;}
    public void setKName(String kName) {this.kName = kName;}
    public void setEName(String eName) {this.eName = eName;}
    public void setPart(String part) {this.part = part;}

    @Override
    public String toString() {
        return "이름: " + this.kName +", name: " + this.eName + ", 역할: " + this.part;
    }
}
