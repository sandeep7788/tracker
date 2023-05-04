package com.vline;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vline.helper.Utility;

import java.util.ArrayList;
import java.util.List;
public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder> {
    private List<String> moviesList;
    private Context context;
    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView title;
        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.image);
        }
    }
    public MoviesAdapter(List<String> moviesList, Context context) {
        this.moviesList = moviesList;
        this.context = context;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_list, parent, false);
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String movie = moviesList.get(position);

        Glide.with(context)
                .load(movie)
                .into(holder.title);

    }
    @Override
    public int getItemCount() {
        return moviesList.size();
    }

    public void setItems(ArrayList<String> newArticles) {
        //get the current items
        int currentSize = moviesList.size();
        //remove the current items
        moviesList.clear();
        //add all the new items
        moviesList.addAll(newArticles);
        //tell the recycler view that all the old items are gone
        notifyItemRangeRemoved(0, currentSize);
        //tell the recycler view how many new items we added
        notifyItemRangeInserted(0, newArticles.size());


    }
}