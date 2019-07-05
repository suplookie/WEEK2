package com.example.week2;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class Choosemenu extends AppCompatActivity {

    //ArrayList<ItemObject> list = new ArrayList<>();
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_menu);
        //listView = findViewById(R.id.today_menu);
        TextView textView = findViewById(R.id.title);
        textView.setText("WEEK2 : "+ ThirdFragment.name);
        //new Description().execute();
/*
        if (ThirdFragment.name.equals("카이마루")) {
            north();
        }
        if (ThirdFragment.name.equals("동맛골")) {
            east();
        }
        if (ThirdFragment.name.equals("서맛골")) {
            west();
        }
        if (ThirdFragment.name.equals("교수회관")) {
            north_prof();
        }
        if (ThirdFragment.name.equals("동맛골 교직원식당")) {
            east_prof();
        }*/

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

    private void east_prof() {
        //웹크롤링, 메뉴 개수 알아오기
        //이름, 내용, 별점, 클릭시 dialog
        //dialog에는 자세한 메뉴/이름, 별점 선택 가능
        //dialog 닫으면 새로운 별점 계산
    }

    private void north_prof() {

    }

    private void west() {

    }

    private void east() {

    }

    private void north() {

    }



}