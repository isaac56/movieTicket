package com.tistory.webnautes.mymovie.RecyclerViews.commentRecyclerView;

/**
 * Created by isaac on 2018-10-25.
 */

public class CommentRecyclerViewItem {
    public String comment;
    public String user;
    public String time;

    public CommentRecyclerViewItem (String comment, String user, String time) {
        this.comment = comment;
        this.user = user;
        this.time = time;
    }
}
