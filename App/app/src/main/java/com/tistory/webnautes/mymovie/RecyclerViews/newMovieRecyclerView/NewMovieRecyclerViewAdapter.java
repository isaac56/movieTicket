package com.tistory.webnautes.mymovie.RecyclerViews.newMovieRecyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tistory.webnautes.mymovie.movieInformation.MovieInformation;
import com.tistory.webnautes.mymovie.R;
import com.tistory.webnautes.mymovie.RecyclerViews.movieRecyclerView.MovieRecyclerViewItem;
import com.tistory.webnautes.mymovie.Static;

import java.util.ArrayList;

/**
 * Created by isaac on 2018-10-15.
 */

public class NewMovieRecyclerViewAdapter extends RecyclerView.Adapter <NewMovieRecyclerViewAdapter.ViewHolder> {
    private ArrayList<MovieRecyclerViewItem> recyclerItems;
    private int layoutPosition;

    public NewMovieRecyclerViewAdapter () {
        this.recyclerItems = new ArrayList<MovieRecyclerViewItem>();
    }

    @Override
    public NewMovieRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_movie_recyclerview_item,parent,false);

        NewMovieRecyclerViewAdapter.ViewHolder holder = new NewMovieRecyclerViewAdapter.ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(NewMovieRecyclerViewAdapter.ViewHolder holder, int position) {
        MovieRecyclerViewItem Item = recyclerItems.get(position);
        MovieInformation movieInfo = Item.getMovieInfo();

        // 아이템 내 각 위젯에 데이터 반영
        if(Item.getBitmapImage() != null)
            holder.posterImageView.setImageBitmap(Item.getBitmapImage());
        else if(Item.getDrawableImage() != null)
            holder.posterImageView.setImageDrawable(Item.getDrawableImage());

        holder.titleTextView.setText(Item.getTitle());
        holder.titleTextView.setSelected(true);
        holder.yearTextView.setText(Item.getYear());
        holder.yearTextView.setSelected(true);
    }

    @Override
    public int getItemCount() {
        return recyclerItems.size();
    }

    public int getLayoutPosition() { return layoutPosition; }

    public void setData(ArrayList<MovieRecyclerViewItem> list) {
        recyclerItems = list;
    }

    public void addItem(MovieRecyclerViewItem item) {
        recyclerItems.add(item);
    }

    public MovieRecyclerViewItem getItem(int position) {return recyclerItems.get(position); }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        ImageView posterImageView;
        TextView titleTextView;
        TextView yearTextView;

        public ViewHolder(View view) {
            super(view);

            view.setOnCreateContextMenuListener(this);
            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            posterImageView = (ImageView) view.findViewById(R.id.moviePoster_imageView) ;
            titleTextView = (TextView) view.findViewById(R.id.movieTitle_textView) ;
            yearTextView = (TextView) view.findViewById(R.id.movieYear_textView) ;
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            Static.recommendTab_which_recyclerView = 0;
            layoutPosition = getLayoutPosition();
            menu.add(0, 0, 0, "볼 영화 등록");
        }
    }
}
