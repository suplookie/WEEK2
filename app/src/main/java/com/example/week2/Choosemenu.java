package com.example.week2;

import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class Choosemenu extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_menu);
        //listView = findViewById(R.id.today_menu);
        TextView textView = findViewById(R.id.title);
        textView.setText("WEEK2 : "+ ThirdFragment.name);
        TextView menu = findViewById(R.id.menu);
        menu.setText(ThirdFragment.cafeteria[ThirdFragment.cafe].menu);

        RatingBar ratingBar = findViewById(R.id.stars);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                ThirdFragment.cafeteria[ThirdFragment.cafe].mrate = v;
            }
        });
    }
}