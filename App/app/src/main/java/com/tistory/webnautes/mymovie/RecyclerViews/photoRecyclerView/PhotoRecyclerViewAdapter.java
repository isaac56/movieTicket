package com.tistory.webnautes.mymovie.RecyclerViews.photoRecyclerView;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tistory.webnautes.mymovie.R;
import com.tistory.webnautes.mymovie.RadiusImageView;

import java.util.ArrayList;

/**
 * Created by isaac on 2018-10-15.
 */

public class PhotoRecyclerViewAdapter extends RecyclerView.Adapter <PhotoRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Bitmap> recyclerItems;

    public PhotoRecyclerViewAdapter () {
        this.recyclerItems = new ArrayList<Bitmap>();
    }

    @Override
    public PhotoRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_recyclerview_item,parent,false);

        PhotoRecyclerViewAdapter.ViewHolder holder = new PhotoRecyclerViewAdapter.ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(PhotoRecyclerViewAdapter.ViewHolder holder, int position) {
        Bitmap item = recyclerItems.get(position);

        holder.moviePhoto_ImageView.setImageBitmap(item);
    }

    @Override
    public int getItemCount() {
        return recyclerItems.size();
    }

    public void setData(ArrayList<Bitmap> list) {
        recyclerItems = list;
    }

    public void addItem(Bitmap item) { recyclerItems.add(item); }

    public Bitmap getItem(int position) {return recyclerItems.get(position); }

    public ArrayList<Bitmap> getItems() { return recyclerItems; }
    
    public class ViewHolder extends RecyclerView.ViewHolder {

        RadiusImageView moviePhoto_ImageView;

        ViewHolder(View view) {
            super(view);

            moviePhoto_ImageView = (RadiusImageView) view.findViewById(R.id.moviePhoto_imageView);
        }
    }
}
