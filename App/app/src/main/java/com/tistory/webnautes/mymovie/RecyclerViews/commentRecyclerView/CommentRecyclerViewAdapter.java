package com.tistory.webnautes.mymovie.RecyclerViews.commentRecyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tistory.webnautes.mymovie.R;
import com.tistory.webnautes.mymovie.RadiusImageView;

import java.util.ArrayList;

/**
 * Created by isaac on 2018-10-15.
 */

public class CommentRecyclerViewAdapter extends RecyclerView.Adapter <CommentRecyclerViewAdapter.ViewHolder> {
    private ArrayList<CommentRecyclerViewItem> recyclerItems;

    public CommentRecyclerViewAdapter() {
        this.recyclerItems = new ArrayList<CommentRecyclerViewItem>();
    }

    @Override
    public CommentRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_recyclerview_item,parent,false);

        CommentRecyclerViewAdapter.ViewHolder holder = new CommentRecyclerViewAdapter.ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(CommentRecyclerViewAdapter.ViewHolder holder, int position) {
        CommentRecyclerViewItem item = recyclerItems.get(position);

        holder.comment_textView.setText(item.comment);
        holder.user_time_textView.setText(item.time + " , "+"아이디: "+ item.user);
    }

    @Override
    public int getItemCount() {
        return recyclerItems.size();
    }

    public void setData(ArrayList<CommentRecyclerViewItem> list) {
        recyclerItems = list;
    }

    public void addItem(CommentRecyclerViewItem item) { recyclerItems.add(item); }

    public void clearItem() { recyclerItems.clear(); }

    public CommentRecyclerViewItem getItem(int position) {return recyclerItems.get(position); }

    public ArrayList<CommentRecyclerViewItem> getItems() { return recyclerItems; }
    
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView comment_textView;
        TextView user_time_textView;

        ViewHolder(View view) {
            super(view);

            comment_textView = (TextView) view.findViewById(R.id.comment_textView);
            user_time_textView = (TextView) view.findViewById(R.id.user_time_textView);
        }
    }
}
