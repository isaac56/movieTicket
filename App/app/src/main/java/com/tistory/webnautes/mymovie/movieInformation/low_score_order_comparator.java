package com.tistory.webnautes.mymovie.movieInformation;

import java.util.Comparator;

/**
 * Created by isaac on 2018-11-22.
 */

public class low_score_order_comparator implements Comparator<MovieInformation> {
    @Override
    public int compare(MovieInformation m1, MovieInformation m2) {
        if(m1.getExpectingScore() > m2.getExpectingScore()) {
            return 1;
        } else if(m1.getExpectingScore() == m2.getExpectingScore()) {
            return 0;
        } else {
            return -1;
        }
    }
}
