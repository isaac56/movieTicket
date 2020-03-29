package com.tistory.webnautes.mymovie.RecyclerViews.personRecyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tistory.webnautes.mymovie.PersonInformation;
import com.tistory.webnautes.mymovie.R;
import com.tistory.webnautes.mymovie.RadiusImageView;

import java.util.ArrayList;

/**
 * Created by isaac on 2018-10-15.
 */

public class PersonRecyclerViewAdapter extends RecyclerView.Adapter <PersonRecyclerViewAdapter.ViewHolder> {
    private ArrayList<PersonInformation> recyclerItems;

    public PersonRecyclerViewAdapter() {
        this.recyclerItems = new ArrayList<PersonInformation>();
    }

    @Override
    public PersonRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.person_recyclerview_item,parent,false);

        PersonRecyclerViewAdapter.ViewHolder holder = new PersonRecyclerViewAdapter.ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(PersonRecyclerViewAdapter.ViewHolder holder, int position) {
        PersonInformation item = recyclerItems.get(position);

        holder.personImage_ImageView.setImageBitmap(item.getImage());
        holder.kName_textView.setText(item.getKName());
        if(item.getEName().compareTo("") == 0) {
            holder.eName_textView.setText(item.getPart());
            holder.part_textView.setText("");
        } else {
            holder.eName_textView.setText(item.getEName());
            holder.part_textView.setText(item.getPart());
        }
    }

    public void setData(ArrayList<PersonInformation> list) {
        recyclerItems = list;
    }

    public void addItem(PersonInformation item) { recyclerItems.add(item); }

    @Override
    public int getItemCount() {
        return recyclerItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        RadiusImageView personImage_ImageView;
        TextView kName_textView;
        TextView eName_textView;
        TextView part_textView;

        ViewHolder(View view) {
            super(view);

            personImage_ImageView = (RadiusImageView) view.findViewById(R.id.personImage_imageView);
            kName_textView = (TextView) view.findViewById(R.id.kName_textView);
            eName_textView = (TextView) view.findViewById(R.id.eName_textView);
            part_textView = (TextView) view.findViewById(R.id.part_textView);
        }
    }
}
