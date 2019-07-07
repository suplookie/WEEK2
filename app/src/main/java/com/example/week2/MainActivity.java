package com.example.week2;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;


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

public class MainActivity extends AppCompatActivity {

    TextView mResult;

    public static String user;


    private static final String TAG = "ProximityTest";
    private final String POI_REACHED =              // 공중파 방송의 채널 같은 역할. 임의로 정함.
            "com.example.proximitytest.POI_REACHED";    //
    private PendingIntent proximityIntent;

    private final double sampleLatitude = 37.5;  // 목표 위치
    private final double sampleLongitude = 127;


    @Override
    protected void onStop() {
        super.onStop();
    }

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupProximityAlert();  //방송국

        mResult = findViewById(R.id.txt_delete_account);


        TextView register = findViewById(R.id.txt_create_account);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View register_layout = LayoutInflater.from(MainActivity.this).inflate(R.layout.register_layout, null);
                new MaterialStyledDialog.Builder(MainActivity.this)
                        .setTitle("REGISTRATION")
                        .setDescription("Please fill out all fields")
                        .setCustomView(register_layout).setNegativeText("CANCEL")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveText("REGISTER")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                MaterialEditText edt_register_password = register_layout.findViewById(R.id.edit_password);
                                MaterialEditText edt_register_name = register_layout.findViewById(R.id.edit_name);

                                if (TextUtils.isEmpty(edt_register_password.getText().toString())) {
                                    Toast.makeText(MainActivity.this, "Password cannot be null or empty", Toast.LENGTH_SHORT).show();
                                    return;
                                } else if (TextUtils.isEmpty(edt_register_name.getText().toString())) {
                                    Toast.makeText(MainActivity.this, "Name cannot be null or empty", Toast.LENGTH_SHORT).show();
                                    return;
                                } else {
                                    new Register().execute("http://143.248.36.204:8080/register", edt_register_name.getText().toString(), edt_register_password.getText().toString());
                                }
                            }
                        }).show();
            }
        });

        final EditText name = findViewById(R.id.edit_name);
        final EditText password = findViewById(R.id.edit_password);

        ProfileTracker profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                successFB();
            }
        };
        profileTracker.startTracking();

        callbackManager = CallbackManager.Factory.create();
        LoginButton facebook = findViewById(R.id.facebook);
        facebook.setReadPermissions("email");

        facebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //successFB();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });


        Button button = findViewById(R.id.button_login);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user = name.getText().toString();
                new PostDataTask().execute("http://143.248.36.204:8080/login", name.getText().toString(), password.getText().toString());
            }
        });

    }

    void successFB() {
        user = Profile.getCurrentProfile().getName();
        new Register().execute("http://143.248.36.204:8080/register", user, "facebook");
        new PostDataTask().execute("http://143.248.36.204:8080/login", user, "facebook");
    }

    private void setupProximityAlert() {
        LocationManager locationManager = (LocationManager)
                getSystemService(LOCATION_SERVICE);

        Log.d(TAG, "start");

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "Registering ProximityAlert");
            //방송 시작, 방송이름은 POI_REACHED, 누가 이방송을 필요하는지는 관심없음. 그냥 보내는...
            Intent intent = new Intent(POI_REACHED);
            proximityIntent =
                    PendingIntent.getBroadcast(MainActivity.this, 0, intent,
                            PendingIntent.FLAG_ONE_SHOT);
            //방송 조건, 목표위치에 50미터 안으로 이동하면 10초간 경보 라는 방송을 보냄. 방송이름은POI_REACHED
            //경보란게 먼지 모르겠음. 에뮬레이터에서 어떻게 확인 가능한지 모름.
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                return;
            }
            locationManager.addProximityAlert(sampleLatitude,
                    sampleLongitude, 50, 10000,
                    proximityIntent);

            /*================================================================*/
            //시청자. POI_REACHED 이란 채널명으로 방송된 내용을 보려고 함.
            IntentFilter intentFilter = new IntentFilter(POI_REACHED);
            registerReceiver(new ProximityAlertReceiver(),
                    intentFilter);
            /*================================================================*/
        } else {
            Log.d(TAG, "GPS_PROVIDER not available");
        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    class GetDataTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
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

            mResult.setText(result);

            if (progressDialog != null) {
                progressDialog.dismiss();
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

    class PostDataTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Inserting data..");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                return postData(params[0], params[1], params[2]);
            } catch (IOException ex) {
                return "Network error !";
            } catch (JSONException ex) {
                return "Data invalid !";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (result.equals("\"" + user + "\"\n")) {
                Intent intent = new Intent(getApplicationContext(), TabActivity.class);
                Toast.makeText(getApplicationContext(), "Hello " + user, Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }

        }

        private String postData(String urlPath, String userName, String password) throws IOException, JSONException {

            StringBuilder result = new StringBuilder();
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader = null;

            try {
                //Create data to send
                JSONObject dataToSend = new JSONObject();
                dataToSend.put("userName", userName);
                dataToSend.put("password", password);

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

    class Register extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Inserting data..");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                return postData(params[0], params[1], params[2]);
            } catch (IOException ex) {
                return "Network error !";
            } catch (JSONException ex) {
                return "Data invalid !";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //mResult.setText(result);

            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        private String postData(String urlPath, String userName, String password) throws IOException, JSONException {

            StringBuilder result = new StringBuilder();
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader = null;

            try {
                //Create data to send
                JSONObject dataToSend = new JSONObject();
                dataToSend.put("userName", userName);
                dataToSend.put("password", password);

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
}



class ProximityAlertReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        //방송을 잘 잡으면 밑에 로그 한번 찍어줌.
        // 지피에스 위치가 변해서 127, 37.5 로 되면 DDMS 에 아래 로그가 찍힘으로 확인 가능
        Log.d("MyTag", "Proximity Alert was fired");
        Toast.makeText(context, "gps received", Toast.LENGTH_SHORT).show();
    }
}

