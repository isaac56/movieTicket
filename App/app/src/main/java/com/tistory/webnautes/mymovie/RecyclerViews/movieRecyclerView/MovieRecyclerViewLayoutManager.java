package com.tistory.webnautes.mymovie.RecyclerViews.movieRecyclerView;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Created by isaac on 2018-10-17.
 */

public class MovieRecyclerViewLayoutManager extends LinearLayoutManager {
    private boolean isScrollEnabled = true;

    public MovieRecyclerViewLayoutManager(Context context) {
        super(context);
    }

    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }
    @Override
    public boolean canScrollVertically() {
        return isScrollEnabled && super.canScrollVertically();
    }
}
