package com.tistory.webnautes.mymovie.recommendTab;

import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.tistory.webnautes.mymovie.movieInformation.MovieInformation;

import java.util.Stack;

public class RecommendTabGroup extends ActivityGroup {
    public static RecommendTabGroup tabGroup;
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

        Intent intent = new Intent(getApplicationContext(), RecommendActivity.class);
        View RecommendView = getLocalActivityManager()
                .startActivity("RecommendActivity", intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                .getDecorView();
        replaceView(RecommendView);
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