package com.example.week2;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MyAdapter extends BaseAdapter {
    Context mContext;
    LayoutInflater inflater = null;

    public MyAdapter(Context context, ItemData[] cafeteria) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            if (inflater == null)
                inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.third_item_layout, viewGroup, false);
        }

        TextView name = view.findViewById(R.id.cafe_name);
        name.setText(ThirdFragment.cafeteria[i].name);
        RelativeLayout layout = view.findViewById(R.id.parent_layout);
        layout.setOnClickListener(ThirdFragment.cafeteria[i].onClickListener);

        countStars(ThirdFragment.cafeteria[i].mrate, view);
        TextView number = view.findViewById(R.id.number);
        number.setText("(" + ThirdFragment.cafeteria[i].number + ")");

        return view;
    }

    void countStars(double rate, View view) {

        ImageView star = view.findViewById(R.id.star_1);
        star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.border_star));
        star = view.findViewById(R.id.star_2);
        star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.border_star));
        star = view.findViewById(R.id.star_3);
        star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.border_star));
        star = view.findViewById(R.id.star_4);
        star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.border_star));
        star = view.findViewById(R.id.star_5);
        star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.border_star));

        if (rate >= 0.5) {
            star = view.findViewById(R.id.star_1);
            star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.half_star));
        }
        if (rate >= 1.0) {
            star = view.findViewById(R.id.star_1);
            star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.full_star));
        }
        if (rate >= 1.5) {
            star = view.findViewById(R.id.star_2);
            star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.half_star));
        }
        if (rate >= 2.0) {
            star = view.findViewById(R.id.star_2);
            star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.full_star));
        }
        if (rate >= 2.5) {
            star = view.findViewById(R.id.star_3);
            star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.half_star));
        }
        if (rate >= 3.0) {
            star = view.findViewById(R.id.star_3);
            star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.full_star));
        }
        if (rate >= 3.5) {
            star = view.findViewById(R.id.star_4);
            star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.half_star));
        }
        if (rate >= 4.0) {
            star = view.findViewById(R.id.star_4);
            star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.full_star));
        }
        if (rate >= 4.5) {
            star = view.findViewById(R.id.star_5);
            star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.half_star));
        }
        if (rate >= 5.0) {
            star = view.findViewById(R.id.star_5);
            star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.full_star));
        }
    }
}
