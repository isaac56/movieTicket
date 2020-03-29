package com.tistory.webnautes.mymovie.RecyclerViews.movieRecyclerView;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.tistory.webnautes.mymovie.movieInformation.MovieInformation;

/**
 * Created by isaac on 2018-10-02.
 */

public class MovieRecyclerViewItem {
    private Bitmap bitmapImage ;
    private Drawable drawableImage;
    private String titleStr;
    private String yearStr;
    private MovieInformation movieInfo;

    public MovieRecyclerViewItem (Bitmap image, String title, String year, MovieInformation movieInfo) {
        this.bitmapImage = image;
        this.drawableImage = null;
        this.titleStr = title;
        this.yearStr = year;
        this.movieInfo = movieInfo;
    }

    public MovieRecyclerViewItem (Drawable image, String title, String year, MovieInformation movieInfo) {
        this.bitmapImage = null;
        this.drawableImage = image;
        this.titleStr = title;
        this.yearStr = year;
        this.movieInfo = movieInfo;
    }

    public void setBitmapImage(Bitmap image) {
        bitmapImage = image ;
    }

    public void setDrawableImage(Drawable image) {
        drawableImage = image;
    }

    public void setTitle(String title) {
        titleStr = title ;
    }

    public void setYear(String year) {
        yearStr = year ;
    }

    public void setMovieInfo(MovieInformation movieInfo) { this.movieInfo = movieInfo; }

    public Bitmap getBitmapImage() {
        return this.bitmapImage ;
    }

    public Drawable getDrawableImage() { return this.drawableImage ; }

    public String getTitle() {
        return this.titleStr ;
    }

    public String getYear() {
        return this.yearStr ;
    }

    public MovieInformation getMovieInfo() { return this.movieInfo; }
}
