package com.tistory.webnautes.mymovie;

import com.tistory.webnautes.mymovie.movieInformation.MovieInformation;
import com.tistory.webnautes.mymovie.ratingTab.RatingActivity;

/**
 * Created by isaac on 2018-09-07.
 */

public class Static {
    public static String url = "http://isaacProject.iptime.org:8000/";
    public static MovieInformation recommendTabMovieInfo;
    public static MovieInformation ratingTabMovieInfo;
    public static MovieInformation wishListTabMovieInfo;
    public static int u_id = 1;


    public static int recommendTab_which_recyclerView;
    public static RatingActivity ratingActivity = null;
}
