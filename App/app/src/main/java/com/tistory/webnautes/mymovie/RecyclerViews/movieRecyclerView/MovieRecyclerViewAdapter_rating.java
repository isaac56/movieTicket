package com.tistory.webnautes.mymovie.RecyclerViews.movieRecyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.tistory.webnautes.mymovie.movieInformation.MovieInformation;
import com.tistory.webnautes.mymovie.R;
import com.tistory.webnautes.mymovie.RadiusImageView;

import java.util.ArrayList;

/**
 * Created by isaac on 2018-10-15.
 */

public class MovieRecyclerViewAdapter_rating extends RecyclerView.Adapter <MovieRecyclerViewAdapter_rating.ViewHolder> {
    private ArrayList<MovieRecyclerViewItem> recyclerItems;
    private OnRecordEventListener clickListener;
    private int layoutPosition;

    public MovieRecyclerViewAdapter_rating() {
        this.recyclerItems = new ArrayList<MovieRecyclerViewItem>();
    }

    public MovieRecyclerViewAdapter_rating(OnRecordEventListener listener) {
        this.recyclerItems = new ArrayList<MovieRecyclerViewItem>();
        clickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_recyclerview_item_rating, parent, false);

        ViewHolder holder = new ViewHolder(view,clickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(MovieRecyclerViewAdapter_rating.ViewHolder holder, int position) {
        MovieRecyclerViewItem Item = recyclerItems.get(position);
        MovieInformation movieInfo = Item.getMovieInfo();

        // 아이템 내 각 위젯에 데이터 반영
        if (Item.getBitmapImage() != null)
            holder.posterImageView.setImageBitmap(Item.getBitmapImage());
        else if (Item.getDrawableImage() != null)
            holder.posterImageView.setImageDrawable(Item.getDrawableImage());

        holder.titleTextView.setText(Item.getTitle());
        holder.yearTextView.setText(Item.getYear());
        if(movieInfo.getScore() == -1)
            holder.scoreTextView.setText("나의평점: 없음");
        else
            holder.scoreTextView.setText("나의평점: " + movieInfo.getScore());
        holder.ratingBar.setRating(movieInfo.getScore());
    }

    @Override
    public int getItemCount() {
        return recyclerItems.size();
    }

    public int getLayoutPosition() { return layoutPosition; }

    public void setItemScore(int m_id, float score) {
        for(MovieRecyclerViewItem item : recyclerItems) {
            if(item.getMovieInfo().getM_id() == m_id) {
                item.getMovieInfo().setScore(score);
                return;
            }
        }
    }

    public void setData(ArrayList<MovieRecyclerViewItem> list) {
        recyclerItems = list;
    }

    public void addItem(MovieRecyclerViewItem item) {
        recyclerItems.add(item);
    }

    public void clearItems() { recyclerItems.clear(); }

    public MovieRecyclerViewItem getItem(int position) {
        return recyclerItems.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        RadiusImageView posterImageView;
        TextView titleTextView;
        TextView yearTextView;
        TextView scoreTextView;
        RatingBar ratingBar;

        public ViewHolder(View view, final OnRecordEventListener listener) {
            super(view);

            view.setOnCreateContextMenuListener(this);
            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            posterImageView = (RadiusImageView) view.findViewById(R.id.poster_imageView);
            titleTextView = (TextView) view.findViewById(R.id.title_textView);
            yearTextView = (TextView) view.findViewById(R.id.year_textView);
            scoreTextView = (TextView) view.findViewById(R.id.score_textView);
            ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if (fromUser) {
                        if(listener != null)
                            listener.onRatingBarChange(recyclerItems.get(getLayoutPosition()), rating);
                    }
                }
            });
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            layoutPosition = getLayoutPosition();
            menu.add(0, 0, 0, "볼 영화 등록");
        }
    }
}
