package com.example.week2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ThirdFragment extends Fragment {

    public static ThirdFragment newInstance() {
        Bundle args = new Bundle();
        ThirdFragment fragment = new ThirdFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Context mContext;

    private Activity activity;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        if (context instanceof Activity)
            activity = (Activity) context;
    }

    private static final String TAG = "ThirdFragment";

    static String name;

    ListView listView;

    static int cafe;
    MyAdapter adapter;



    static final ItemData[] cafeteria = new ItemData[5];
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        cafeteria[0] = new ItemData("동맛골", 1.2);
        cafeteria[1] = new ItemData("서맛골", 2.8);
        cafeteria[2] = new ItemData("카이마루", 0.78);
        cafeteria[3] = new ItemData("교수회관", 3.8);
        cafeteria[4] = new ItemData("동맛골 교직원식당", 2.2);

        new ThirdFragment.Description().execute();

        for (int k = 0; k < 5; k++) {
            final int tmp = k;
            cafeteria[k].onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    name = cafeteria[tmp].name;
                    cafe = tmp;
                    Toast.makeText(mContext, name, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(mContext, Choosemenu.class);
                    startActivityForResult(intent, tmp);
                }
            };
        }
        Log.d(TAG, "onCreateView:started.");
        View view = inflater.inflate(R.layout.fragment_third, container, false);
        listView = view.findViewById(R.id.list_cafeteria) ;
        adapter = new MyAdapter(mContext, cafeteria);
        listView.setAdapter(adapter);
        return view;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        adapter.notifyDataSetChanged();
    }

    private class Description extends AsyncTask<Void, Void, Void> {

        //진행바표시
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //진행다일로그 시작
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("잠시 기다려 주세요.");
            progressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect("https://bds.bablabs.com/restaurants?campus_id=JEnfpqCUuR").get();
                Elements mElementDataSize = doc.select("div[class=restaurants-list]").select("div[class=restaurant-item]"); //필요한 녀석만 꼬집어서 지정
                int i = 0;

                for(Element elem : mElementDataSize){ //이렇게 요긴한 기능이
                    if (i == 5) break;

                    Elements elem1 = elem.select("div[class=card-title]");
                    String menu = elem1.text();
                    cafeteria[i].menu = menu;
                    i++;
                }

                Log.d("debug :", "List " + mElementDataSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //ArraList를 인자로 해서 어답터와 연결한다.

            listView.setAdapter(adapter);

            progressDialog.dismiss();
        }
    }
}

class ItemData{
    public String name;
    public double mrate;
    public View.OnClickListener onClickListener;
    public String menu;

    ItemData(String name, double rate) {
        this.name = name;
        this.mrate = rate;
        this.onClickListener = null;
    }
}

