package com.example.week2;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class Choosemenu extends AppCompatActivity{

    float rate;
    static ArrayList<String> names;
    static ArrayList<Float> rates;
    static ArrayList<String> reviews;
    RatingBar ratingBar;
    final ReviewAdapter adapter = new ReviewAdapter(Choosemenu.this);
    EditText review;
    TextView total;

    boolean written = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_menu);
        names = new ArrayList<>();
        rates = new ArrayList<>();
        reviews = new ArrayList<>();

        int i = ThirdFragment.cafe + 1;

        new getReviews().execute("http://143.248.36.204:8080/place" + i + "/reviews");
        ratingBar = findViewById(R.id.stars);
        review = findViewById(R.id.review);

        TextView textView = findViewById(R.id.title);
        textView.setText("WEEK2 : "+ ThirdFragment.name);

        TextView menu = findViewById(R.id.menu);
        menu.setText(ThirdFragment.cafeteria[ThirdFragment.cafe].menu);

        total = findViewById(R.id.total);
        total.setText("Total Rate: " + ThirdFragment.cafeteria[ThirdFragment.cafe].mrate);

        RecyclerView recyclerView = findViewById(R.id.recycle);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(Choosemenu.this));


        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(final RatingBar ratingBar, final float v, boolean b) {
                rate = v;
                Button button = findViewById(R.id.submit);
                for (int p = 0; p < names.size(); p++) {
                    if (names.get(p).equals(MainActivity.user)) {
                        written = true;
                        break;
                    }
                }
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!written) {
                            Toast.makeText(Choosemenu.this, "rate submitted", Toast.LENGTH_SHORT).show();
                            String num = ThirdFragment.cafeteria[ThirdFragment.cafe].number;
                            ThirdFragment.cafeteria[ThirdFragment.cafe].number = String.valueOf(Integer.valueOf(num) + 1);
                            ThirdFragment.adapter.notifyDataSetChanged();
                            int i = ThirdFragment.cafe + 1;
                            new PostReview().execute("http://143.248.36.204:8080/place" + i + "/reviews", MainActivity.user, String.valueOf(rate), review.getText().toString());
                            written = true;
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "You already posted a review", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }


    class PostReview extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Choosemenu.this);
            progressDialog.setMessage("Inserting data..");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                return postData(params[0], params[1], params[2], params[3]);
            } catch (IOException ex) {
                return "Network error !";
            } catch (JSONException ex) {
                return "Data invalid !";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //내 별점과 리뷰를 보내고,
            super.onPostExecute(result);

            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            float res = (float) ((ThirdFragment.cafeteria[ThirdFragment.cafe].mrate * rates.size() + rate)/(rates.size() + 1));
            ThirdFragment.cafeteria[ThirdFragment.cafe].mrate = res;
            ThirdFragment.adapter.notifyDataSetChanged();
            total.setText("Total Rate: " + res);
            ratingBar.setRating(rate);
            names.add(MainActivity.user);
            rates.add(rate);
            reviews.add(review.getText().toString());
            adapter.notifyDataSetChanged();

        }

        private String postData(String urlPath, String user, String rate, String review) throws IOException, JSONException {

            StringBuilder result = new StringBuilder();
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader = null;

            try {
                //Create data to send
                JSONObject dataToSend = new JSONObject();
                dataToSend.put("userName", user);
                dataToSend.put("rating", rate);
                dataToSend.put("content", review);

                //Init and config request, then connect to server
                URL url = new URL(urlPath);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true); //enable output
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.connect();

                //Write data into server
                OutputStream outputStream = urlConnection.getOutputStream();
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                bufferedWriter.write(dataToSend.toString());
                bufferedWriter.flush();

                //Read data response from server
                InputStream inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            }
            return result.toString();
        }
    }
    class getReviews extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Choosemenu.this);
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
                    names.add(order.getString("userName"));
                    rates.add(Float.valueOf(order.getString(("rating"))));
                    reviews.add(order.getString("content"));
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