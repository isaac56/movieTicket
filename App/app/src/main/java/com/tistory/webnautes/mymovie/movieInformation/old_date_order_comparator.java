package com.tistory.webnautes.mymovie.movieInformation;

import java.util.Comparator;

/**
 * Created by isaac on 2018-11-22.
 */

public class old_date_order_comparator implements Comparator<MovieInformation> {
    @Override
    public int compare(MovieInformation m1, MovieInformation m2) {
        return m1.getYearFromTitle().compareTo(m2.getYearFromTitle());
    }
}
