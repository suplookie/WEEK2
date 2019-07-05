package com.example.week2;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class MyAdapter extends BaseAdapter {
    ItemData[] stringArray;
    Context mContext;
    LayoutInflater inflater = null;

    public MyAdapter(Context context, ItemData[] cafeteria) {
        stringArray = cafeteria;
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
        name.setText(stringArray[i].name);
        RelativeLayout layout = view.findViewById(R.id.parent_layout);
        layout.setOnClickListener(stringArray[i].onClickListener);

        if (stringArray[i].mrate > 0) {
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

            if (stringArray[i].mrate >= 0.5) {
                star = view.findViewById(R.id.star_1);
                star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.half_star));
            }
            if (stringArray[i].mrate >= 1.0) {
                star = view.findViewById(R.id.star_1);
                star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.full_star));
            }
            if (stringArray[i].mrate >= 1.5) {
                star = view.findViewById(R.id.star_2);
                star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.half_star));
            }
            if (stringArray[i].mrate >= 2.0) {
                star = view.findViewById(R.id.star_2);
                star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.full_star));
            }
            if (stringArray[i].mrate >= 2.5) {
                star = view.findViewById(R.id.star_3);
                star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.half_star));
            }
            if (stringArray[i].mrate >= 3.0) {
                star = view.findViewById(R.id.star_3);
                star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.full_star));
            }
            if (stringArray[i].mrate >= 3.5) {
                star = view.findViewById(R.id.star_4);
                star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.half_star));
            }
            if (stringArray[i].mrate >= 4.0) {
                star = view.findViewById(R.id.star_4);
                star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.full_star));
            }
            if (stringArray[i].mrate >= 4.5) {
                star = view.findViewById(R.id.star_5);
                star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.half_star));
            }
            if (stringArray[i].mrate >= 5.0) {
                star = view.findViewById(R.id.star_5);
                star.setImageDrawable(mContext.getResources().getDrawable(R.drawable.full_star));
            }
        }

        return view;
    }
}
