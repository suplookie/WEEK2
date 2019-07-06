package com.example.week2;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;

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


public class FirstFragment extends Fragment {

    private static final int ADD_CONTACTS = 1;

    public static FirstFragment newInstance() {
        Bundle args = new Bundle();
        FirstFragment fragment = new FirstFragment();
        fragment.setArguments(args);
        return new FirstFragment();
    }


    private static final String TAG = "MainActivity";


    //vars
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<Bitmap> mImage = new ArrayList<Bitmap>();
    private ArrayList<String> mPhoneNo = new ArrayList<>();


    FloatingActionButton add_contacts;
    View rootView;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "onCreate:started.");

        rootView = inflater.inflate(R.layout.fragment_first, container, false);

        new getContact().execute("http://143.248.36.204:8080/contacts/" + MainActivity.user);


        final RecyclerView recyclerView = rootView.findViewById(R.id.recyclerv_view);

        final RecyclerViewAdapter adapter = new RecyclerViewAdapter(getActivity(), mNames, mImage, mPhoneNo);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));



        add_contacts = rootView.findViewById(R.id.add_contact);

        LoadContacts();

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        add_contacts.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                final View register_layout = LayoutInflater.from(getContext()).inflate(R.layout.add_contact, null);
                new MaterialStyledDialog.Builder(getContext())
                        .setTitle("ADD CONTACT")
                        .setDescription("Please fill out all fields")
                        .setCustomView(register_layout).setNegativeText("CANCEL")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveText("ADD")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                MaterialEditText edt_register_phone = register_layout.findViewById(R.id.edit_number);
                                MaterialEditText edt_register_name = register_layout.findViewById(R.id. edit_name);

                                if (TextUtils.isEmpty(edt_register_phone.getText().toString())) {
                                    Toast.makeText(getContext(), "Number cannot be null or empty", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                else if (TextUtils.isEmpty(edt_register_name.getText().toString())) {
                                    Toast.makeText(getContext(), "Name cannot be null or empty", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                else {
                                    mNames.add(edt_register_name.getText().toString());
                                    mPhoneNo.add(edt_register_phone.getText().toString());
                                    mImage.add(null);
                                    adapter.notifyDataSetChanged();


                                    new addContact().execute("http://143.248.36.204:8080/contacts/" + MainActivity.user, edt_register_name.getText().toString(), edt_register_phone.getText().toString());
                                }
                            }
                        }).show();
            }
        });


        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            Toast.makeText(getActivity(), "result code: "+ resultCode, Toast.LENGTH_SHORT).show();mImage.clear();
            mPhoneNo.clear();
            mNames.clear();

            final RecyclerView recyclerView = rootView.findViewById(R.id.recyclerv_view);

            final RecyclerViewAdapter adapter = new RecyclerViewAdapter(getActivity(), mNames, mImage, mPhoneNo);

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            LoadContacts();
            new addContact().execute("http://143.248.36.204:8080/contacts/" + MainActivity.user, mNames.get(mNames.size() - 1), mPhoneNo.get(mNames.size() - 1));
    }



    private void LoadContacts(){

        mNames.clear();
        mPhoneNo.clear();
        mImage.clear();

        Log.d(TAG, "iniImageBitmaps : preparing bitmaps.");

        ContentResolver resolver = getActivity().getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null,null, null, null);

        while(cursor.moveToNext()){
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            mNames.add(name);

            Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id},null);


            if (phoneCursor.moveToNext()) {
                String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                mPhoneNo.add(phoneNumber);
            }else {
                mPhoneNo.add("ADD PHONE NUMBER");
            }



            Bitmap photo = BitmapFactory.decodeResource(getActivity().getResources(), R.id.image);

            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getActivity().getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id)));

            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
                mImage.add(photo);
            }else{
                photo = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_launcher_foreground);
                mImage.add(photo);
            }
        }

    }

    class getContact extends AsyncTask<String, Void, String> {

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
            Log.i("연락처", result);

            try{
                JSONArray ja = new JSONArray(result);
                for (int i = 0; i < ja.length(); i++){
                    JSONObject order = ja.getJSONObject(i);
                    mNames.add(order.getString("name"));
                    mPhoneNo.add(order.getString("phoneNumber"));
                    mImage.add(null);
                }
            }
            catch (JSONException e){ ;}

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

    class addContact extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
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

        private String postData(String urlPath, String name, String phoneNumber) throws IOException, JSONException {

            StringBuilder result = new StringBuilder();
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader = null;

            try {
                //Create data to send
                JSONObject dataToSend = new JSONObject();
                dataToSend.put("userName", MainActivity.user);
                dataToSend.put("name", name);
                dataToSend.put("phoneNumber", phoneNumber);

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

    class PostDataTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Inserting data..");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                return postData(params[0]);
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

        private String postData(String urlPath) throws IOException, JSONException {

            StringBuilder result = new StringBuilder();
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader = null;

            try {
                //Create data to send
                JSONObject dataToSend = new JSONObject();
                dataToSend.put("userName", MainActivity.user);
                dataToSend.put("name", "Wuthering Heights");
                dataToSend.put("phoneNumber", "Emily Bronte");

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

