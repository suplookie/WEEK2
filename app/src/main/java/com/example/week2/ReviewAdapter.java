package com.example.week2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private static final String TAG = "ReviewAdapter";
    private Context mContext;


    public ReviewAdapter(Context Context) {
        this.mContext = Context;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView name;
        TextView review;
        RatingBar star;

        public ViewHolder(View itemView){
            super(itemView);
            name = itemView.findViewById(R.id.cafe_name);
            star = itemView.findViewById(R.id.stars);
            review = itemView.findViewById(R.id.review);
        }
    }

    @NonNull
    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_layout, parent, false);
        return new ReviewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        holder.getAdapterPosition();

        holder.name.setText(Choosemenu.names.get(position));
        holder.star.setRating(Choosemenu.rates.get(position));
        holder.review.setText(Choosemenu.reviews.get(position));
    }


    @Override
    public int getItemCount() {
        return Choosemenu.names.size();
    }


}
