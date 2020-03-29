package com.tistory.webnautes.mymovie.movieInformation;

/**
 * Created by isaac on 2018-10-08.
 */

public class MovieInformation {
    private int m_id;
    private String title;
    private String naverMovieCode;
    private float expectingScore;
    private float averageScore;
    private float score;

    public MovieInformation(int m_id, String title, float expectingScore, float averageScore) {
        this.m_id = m_id;
        this.title = title;
        this.naverMovieCode = null;
        this.expectingScore = Math.round(expectingScore*(float)100)/(float)100;
        this.averageScore = Math.round(averageScore*(float)100)/(float)100;
        this.score = -1;
    }

    public MovieInformation(int m_id, String title, float expectingScore, float averageScore, float score) {
        this.m_id = m_id;
        this.title = title;
        this.naverMovieCode = null;
        this.expectingScore = Math.round(expectingScore*(float)100)/(float)100;
        this.averageScore = Math.round(averageScore*(float)100)/(float)100;
        this.score = score;
    }

    public MovieInformation (int m_id, String title) {
        this.m_id = m_id;
        this.title = title;
        this.naverMovieCode = null;
        this.expectingScore = -1;
        this.averageScore = -1;
        this.score = -1;
    }

    public MovieInformation(String naverMovieCode) {
        this.m_id = -1;
        this.title = null;
        this.naverMovieCode = naverMovieCode;
        this.expectingScore = -1;
        this.averageScore = -1;
    }

    public int getM_id() { return m_id; }

    public String getTitle() {
        return title;
    }

    public String getNaverMovieCode() { return naverMovieCode; }

    public float getExpectingScore() {
        return expectingScore;
    }

    public float getAverageScore() {
        return averageScore;
    }

    public float getScore() { return score; }

    public void setM_id(int m_id) {this.m_id = m_id; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setNaverMovieCode(String code) { this.naverMovieCode = code; }

    public void setExpectingScore(float expectingScore) {
        this.expectingScore = expectingScore;
    }

    public void setAverageScore(float averageScore) {
        this.averageScore = averageScore;
    }

    public void setScore(float score) { this.score = score; }

    @Override
    public boolean equals(Object toCompare) {
        MovieInformation movieInfo2 = (MovieInformation)toCompare;
        if(this.m_id == movieInfo2.getM_id())
            return true;
        else
            return false;
    }

    //제목에서 연도 추출
    public String getYearFromTitle() {
        int startIndex, endIndex;
        String year = null;
        try {
            if ((startIndex = title.lastIndexOf('(')) != -1 && (endIndex = title.lastIndexOf(')')) != -1) {
                year = title.substring(startIndex + 1, endIndex);
                return year;
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
