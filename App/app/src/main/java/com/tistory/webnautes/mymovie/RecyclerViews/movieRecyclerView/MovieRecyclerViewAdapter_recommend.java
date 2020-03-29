package com.tistory.webnautes.mymovie.RecyclerViews.movieRecyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.tistory.webnautes.mymovie.movieInformation.MovieInformation;
import com.tistory.webnautes.mymovie.R;
import com.tistory.webnautes.mymovie.RadiusImageView;
import com.tistory.webnautes.mymovie.Static;

import java.util.ArrayList;

/**
 * Created by isaac on 2018-10-15.
 */

public class MovieRecyclerViewAdapter_recommend extends RecyclerView.Adapter <MovieRecyclerViewAdapter_recommend.ViewHolder> {
    private ArrayList<MovieRecyclerViewItem> recyclerItems;
    private int layoutPosition;

    public MovieRecyclerViewAdapter_recommend() {
        this.recyclerItems = new ArrayList<MovieRecyclerViewItem>();
    }

    @Override
    public MovieRecyclerViewAdapter_recommend.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_recyclerview_item_recommend,parent,false);

        MovieRecyclerViewAdapter_recommend.ViewHolder holder = new MovieRecyclerViewAdapter_recommend.ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(MovieRecyclerViewAdapter_recommend.ViewHolder holder, int position) {
        MovieRecyclerViewItem Item = recyclerItems.get(position);
        MovieInformation movieInfo = Item.getMovieInfo();

        // 아이템 내 각 위젯에 데이터 반영
        if(Item.getBitmapImage() != null)
            holder.posterImageView.setImageBitmap(Item.getBitmapImage());
        else if(Item.getDrawableImage() != null)
            holder.posterImageView.setImageDrawable(Item.getDrawableImage());

        holder.titleTextView.setText(Item.getTitle());
        holder.yearTextView.setText(Item.getYear());
        holder.expectingScoreTextView.setText("예상평점: " + movieInfo.getExpectingScore());
        holder.expectingRatingBar.setRating(movieInfo.getExpectingScore());
        holder.averageScoreTextView.setText(": " + movieInfo.getAverageScore());
    }

    public int getLayoutPosition() {
        return layoutPosition;
    }

    @Override
    public int getItemCount() {
        return recyclerItems.size();
    }

    public void setData(ArrayList<MovieRecyclerViewItem> list) {
        recyclerItems = list;
    }

    public void addItem(MovieRecyclerViewItem item) {
        recyclerItems.add(item);
    }

    public void clearItems() { recyclerItems.clear(); }

    public MovieRecyclerViewItem getItem(int position) {return recyclerItems.get(position); }
    
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{

        RadiusImageView posterImageView;
        TextView titleTextView;
        TextView yearTextView;
        TextView expectingScoreTextView;
        RatingBar expectingRatingBar;
        TextView averageScoreTextView;
        ImageView averageScoreImageView;

        public ViewHolder(View view) {
            super(view);

            view.setOnCreateContextMenuListener(this);
            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            posterImageView = (RadiusImageView) view.findViewById(R.id.poster_imageView) ;
            titleTextView = (TextView) view.findViewById(R.id.title_textView) ;
            yearTextView = (TextView) view.findViewById(R.id.year_textView) ;
            expectingScoreTextView = (TextView) view.findViewById(R.id.expectingScore_textView);
            expectingRatingBar = (RatingBar) view.findViewById(R.id.expecting_ratingBar);
            averageScoreTextView = (TextView) view.findViewById(R.id.averageScore_textView);
            averageScoreImageView = (ImageView) view.findViewById(R.id.averageScore_imageView);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            Static.recommendTab_which_recyclerView = 1;
            layoutPosition = getLayoutPosition();
            menu.add(0, 0, 0, "볼 영화 등록");
            //menu.add(0, 1, 0, "추천목록에서 삭제");
        }
    }
}
