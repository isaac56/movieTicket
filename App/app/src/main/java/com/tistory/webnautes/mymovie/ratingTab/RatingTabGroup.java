package com.tistory.webnautes.mymovie.ratingTab;

import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.tistory.webnautes.mymovie.movieInformation.MovieInformation;

import java.util.Stack;

/**
 * Created by isaac on 2018-10-17.
 */

public class RatingTabGroup extends ActivityGroup {
    public static RatingTabGroup tabGroup;
    private Stack<View> history;
    public static Context context;
    public MovieInformation movieInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        history = new Stack<View>();
        tabGroup = this;
        context = this;
        movieInfo = null;

        Intent intent = new Intent(getApplicationContext(), RatingActivity.class);
        View RatingView = getLocalActivityManager()
                .startActivity("RatingActivity", intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                .getDecorView();
        replaceView(RatingView);
    }


    public void replaceView(View view) {
        history.push(view);
        setContentView(view);
    }

    public void back() {
        if(history.size() > 0) {
            history.pop();
            if (history.size() == 0)
                finish();
            else
                setContentView(history.peek());
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        back();
        return;
    }
}