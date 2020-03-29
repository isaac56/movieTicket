package com.tistory.webnautes.mymovie.RecyclerViews.movieRecyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tistory.webnautes.mymovie.movieInformation.MovieInformation;
import com.tistory.webnautes.mymovie.R;
import com.tistory.webnautes.mymovie.RadiusImageView;

import java.util.ArrayList;

/**
 * Created by isaac on 2018-10-15.
 */

public class MovieRecyclerViewAdapter_wishList extends RecyclerView.Adapter <MovieRecyclerViewAdapter_wishList.ViewHolder> {
    private ArrayList<MovieRecyclerViewItem> recyclerItems;
    private int layoutPosition;

    public MovieRecyclerViewAdapter_wishList() {
        this.recyclerItems = new ArrayList<MovieRecyclerViewItem>();
    }

    @Override
    public MovieRecyclerViewAdapter_wishList.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_recyclerview_item_wishlist,parent,false);

        MovieRecyclerViewAdapter_wishList.ViewHolder holder = new MovieRecyclerViewAdapter_wishList.ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(MovieRecyclerViewAdapter_wishList.ViewHolder holder, int position) {
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
    }

    @Override
    public int getItemCount() {
        return recyclerItems.size();
    }

    public int getLayoutPosition() { return  layoutPosition; }

    public void setData(ArrayList<MovieRecyclerViewItem> list) {
        recyclerItems = list;
    }

    public void addItem(MovieRecyclerViewItem item) {
        recyclerItems.add(item);
    }

    public MovieRecyclerViewItem getItem(int position) {return recyclerItems.get(position); }

    public void deleteItem(int position) {
        recyclerItems.remove(position);
        return;
    }

    public void clearItems() {
        recyclerItems.clear();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        RadiusImageView posterImageView;
        TextView titleTextView;
        TextView yearTextView;

        public ViewHolder(View view) {
            super(view);

            view.setOnCreateContextMenuListener(this);
            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            posterImageView = (RadiusImageView) view.findViewById(R.id.poster_imageView) ;
            titleTextView = (TextView) view.findViewById(R.id.title_textView) ;
            yearTextView = (TextView) view.findViewById(R.id.year_textView) ;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            layoutPosition = getLayoutPosition();
            menu.add(0, 0, 0, "목록에서 삭제");
        }
    }
}
