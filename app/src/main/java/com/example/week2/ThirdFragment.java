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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
    static MyAdapter adapter;



    static final ItemData[] cafeteria = new ItemData[5];
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        cafeteria[0] = new ItemData("동맛골", 0);
        cafeteria[1] = new ItemData("서맛골", 0);
        cafeteria[2] = new ItemData("카이마루", 0);
        cafeteria[3] = new ItemData("교수회관", 0);
        cafeteria[4] = new ItemData("동맛골 교직원식당", 0);
        new getRates().execute("http://143.248.36.204:8080/places");

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


    class getRates extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Loading data..");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                return getData(params[0]);
            }
            catch (IOException ex){
                return "Network error !";
            }
        }

        @Override
        protected void onPostExecute (String result) {
            super.onPostExecute(result);

            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            try{
                JSONArray ja = new JSONArray(result);
                for (int i = 0; i < ja.length(); i++){
                    JSONObject order = ja.getJSONObject(i);
                    cafeteria[i].mrate= Float.valueOf(order.getString("average"));
                    cafeteria[i].number = (order.getString("reviewCount"));
                }

            }
            catch (JSONException e){ ;}
        }

        private String getData (String urlPath) throws IOException {
            StringBuilder result = new StringBuilder();
            BufferedReader bufferedReader = null;
            try{
                URL url = new URL(urlPath);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                Log.i("connect", "before");
                urlConnection.connect();
                Log.i("connect", "after");

                //Read response
                InputStream inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) !=  null) {
                    result.append(line).append("\n");
                }
            } finally {
                if(bufferedReader != null) {
                    bufferedReader.close();
                }
            }
            Gson gson = new Gson();
            //result.toJson();
            return result.toString();
        }
    }
}

class ItemData{
    public String name;
    public double mrate;
    public View.OnClickListener onClickListener;
    public String menu;
    String reviews;
    String number;

    ItemData(String name, double rate) {
        this.name = name;
        this.mrate = rate;
        this.onClickListener = null;
    }
}

