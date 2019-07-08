package com.example.week2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class TabActivity extends AppCompatActivity {


    public static ArrayList<Uri> list;


    int idx;
    File file, dir;
    private String savePath= "ImageTemp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(viewPagerAdapter);

        TabLayout tabs= findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                changeView(pos);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


        new getCount().execute("http://143.248.36.204:8080/count/" + MainActivity.user);

        MakePhtoDir();

        for (int i = 0; i < idx; i++) {

            String imgUrl = "http://143.248.36.204:8080/photos/" + MainActivity.user + "/" + i;
            String FileName = imgUrl.substring( imgUrl.lastIndexOf('/')+1, imgUrl.length() );
            DownloadPhotoFromURL downloadPhotoFromURL = new DownloadPhotoFromURL();
            // 동일한 파일이 있는지 검사
            if(!new File(dir.getPath() + File.separator + FileName).exists()){
                downloadPhotoFromURL.execute(imgUrl,FileName);
            } else {
                Toast.makeText(TabActivity.this, "파일이 이미 존재합니다", Toast.LENGTH_SHORT).show();

                File file = new File(dir + "/" + FileName);
                //Bitmap photoBitmap = BitmapFactory.decodeFile(file.getAbsolutePath() );
                //imageView.setImageBitmap(photoBitmap);
                TabActivity.list.add(Uri.parse(file.getPath()));
            }

        }

    }

    private void changeView(int index) {
        switch (index) {
            case 0 :
                break ;
            case 1 :
                break ;
            case 2 :
                break ;
        }
    }


    private void MakePhtoDir(){
        //savePath = "/Android/data/" + getPackageName();
        //dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), savePath);
        dir = new File(Environment.getExternalStorageDirectory(), savePath );
        if (!dir.exists())
            dir.mkdirs(); // make dir
    }

    class getCount extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
            try {
                JSONObject json = new JSONObject(result);
                idx = json.getInt("count");
                Toast.makeText(TabActivity.this, "count: " + idx, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
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

    class DownloadPhotoFromURL extends AsyncTask<String, Integer, String> {
        int count;
        int lenghtOfFile = 0;
        InputStream input = null;
        OutputStream output = null;
        String tempFileName;

        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(TabActivity.this);
            progressDialog.setMessage("Loading data..");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d("background", "아아아아아");
            tempFileName = params[1];
            file = new File(dir, params[1]); // 다운로드할 파일명
            try {
                URL url = new URL(params[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                lenghtOfFile = connection.getContentLength(); // 파일 크기를 가져옴


                input = new BufferedInputStream(url.openStream());
                output = new FileOutputStream(file);
                byte data[] = new byte[1024];
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return String.valueOf(-1);
                    }
                    total = total + count;
                    if (lenghtOfFile > 0) { // 파일 총 크기가 0 보다 크면
                        publishProgress((int) (total * 100 / lenghtOfFile));
                    }
                    output.write(data, 0, count); // 파일에 데이터를 기록
                }

                output.flush();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    }
                    catch(IOException ioex) {
                    }
                }
                if (output != null) {
                    try {
                        output.close();
                    }
                    catch(IOException ioex) {
                    }
                }
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        protected void onPostExecute(String result) {
            // pdLoading.dismiss();
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (result == null) {
                Toast.makeText(TabActivity.this, "다운로드 완료되었습니다.", Toast.LENGTH_LONG).show();

                File file = new File(dir + "/" + tempFileName);
                //이미지 스캔해서 갤러리 업데이트
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                TabActivity.list.add(Uri.fromFile(file));
            } else {
                Toast.makeText(TabActivity.this, "다운로드 에러", Toast.LENGTH_LONG).show();
            }
        }
    }

}
