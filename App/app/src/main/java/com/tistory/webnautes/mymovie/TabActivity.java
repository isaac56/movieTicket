package com.tistory.webnautes.mymovie;
import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.tistory.webnautes.mymovie.ratingTab.RatingTabGroup;
import com.tistory.webnautes.mymovie.recommendTab.RecommendTabGroup;
import com.tistory.webnautes.mymovie.wishListTab.WishListActivity;
import com.tistory.webnautes.mymovie.wishListTab.WishListTabGroup;

public class TabActivity extends ActivityGroup {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        createTab();
    }

    private void createTab() {
        TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup(getLocalActivityManager());

        tabHost.addTab(tabHost.newTabSpec("TAB1").setIndicator("추천받기")
                .setContent(new Intent(this, RecommendTabGroup.class)));
        tabHost.addTab(tabHost.newTabSpec("TAB2").setIndicator("평가하기")
                .setContent(new Intent(this, RatingTabGroup.class)));
        tabHost.addTab(tabHost.newTabSpec("TAB3").setIndicator("볼 영화")
                .setContent(new Intent(this, WishListTabGroup.class)));

    }
}
