package com.example.week2;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;


import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SecondFragment extends Fragment {

    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA= 2;
    private String mCurrentPhotoPath;
    private FloatingActionButton fab_img;
    private FloatingActionButton fab_cam;
    private CardAdapter adapter;
    private boolean isMenuOpen = false;
    private Uri imgUri;
    static FloatingActionButton delete_fab;
    String path;
    String TAG = "sending file";

    public static SecondFragment newInstance() {
        Bundle args = new Bundle();
        SecondFragment fragment = new SecondFragment();
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);
        tedPermission();

        delete_fab = view.findViewById(R.id.fab_delete);
        delete_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                while (!CardAdapter.cards.isEmpty()) {
                    TabActivity.list.remove(CardAdapter.cards.get(0).uri);
                    CardAdapter.cards.remove(0);
                }
                delete_fab.hide();
                adapter.notifyDataSetChanged();
            }
        });


        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        final RecyclerView recyclerView = view.findViewById(R.id.recycle2) ;
        //recyclerView.setLayoutManager(new LinearLayoutManager(activity)) ;


        GridLayoutManager mGridLayoutManager;
        int cols = 3;
        mGridLayoutManager = new GridLayoutManager(mContext, cols);
        recyclerView.setLayoutManager(mGridLayoutManager);

        // 리사이클러뷰에 CardAdapter 객체 지정.
        adapter = new CardAdapter(TabActivity.list) ;

        AppBarLayout appBar = activity.findViewById(R.id.appbar);
        appBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuClose();
            }
        });


        //fab click시 앨범에서 이미지 가져와 리스트에 추가
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab_img = view.findViewById(R.id.fab_img);
        fab_cam = view.findViewById(R.id.fab_cam);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuOpen();
            }
        });

        fab_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectAlbum();
                (adapter).notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(Integer.MAX_VALUE);
                menuClose();
            }
        });

        fab_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
                (adapter).notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(Integer.MAX_VALUE);
                menuClose();
            }
        });

        //리사이클러뷰에 CardAdapter 객체 지정
        recyclerView.setAdapter(adapter) ;

        ImageView imageView = view.findViewById(R.id.img);
        imageView.setImageURI(Uri.parse("http://143.248.36.204:8080/photos/" + MainActivity.user + "/0"));





        adapter.notifyDataSetChanged();


        return view;

    }


    private void takePhoto(){
        // 촬영 후 이미지 가져옴
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(intent.resolveActivity(activity.getPackageManager())!=null){
                File photoFile = null;
                try{
                    photoFile = createImageFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if(photoFile!=null){
                    Uri providerURI = FileProvider.getUriForFile(mContext,activity.getPackageName(),photoFile);
                    imgUri = providerURI;
                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, providerURI);
                    startActivityForResult(intent, PICK_FROM_CAMERA);
                }
            }
        }else{
            Log.v("알림", "저장공간에 접근 불가능");
        }
    }

    private void selectAlbum(){
        //앨범 열기
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_FROM_ALBUM);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK){
            return;
        }

        switch (requestCode){

            case PICK_FROM_ALBUM : {
                //앨범에서 가져오기
                if(data.getData()!=null){
                    try{
                        imgUri = data.getData();
                        //이미지뷰에 이미지 셋팅
                        if (!TabActivity.list.add(imgUri))
                            Toast.makeText(activity, "list add failed", Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                        Log.v("알림","앨범에서 가져오기 에러");
                    }
                }
                break;
            }
            case PICK_FROM_CAMERA: {
                //촬영
                try{
                    Log.v("알림", "FROM_CAMERA 처리");
                    galleryAddPic();
                    //이미지뷰에 이미지셋팅
                    if (!TabActivity.list.add(imgUri))
                        Toast.makeText(activity, "list add failed", Toast.LENGTH_SHORT).show();

                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            }
        }
        if (mCurrentPhotoPath == null) {
            mCurrentPhotoPath = getRealPathFromURI(mContext, imgUri);
        }
        new UploadFile().execute();

        adapter.notifyDataSetChanged();
    }

    private void galleryAddPic(){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        Objects.requireNonNull(activity).sendBroadcast(mediaScanIntent);
        Toast.makeText(mContext,"사진이 저장되었습니다",Toast.LENGTH_SHORT).show();
    }

    private File createImageFile() throws IOException {
        String imgFileName = System.currentTimeMillis() + ".jpg";
        File imageFile;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "ireh");
        if(!storageDir.exists()){
            Log.v("알림","storageDir 존재 x " + storageDir.toString());
            storageDir.mkdirs();
        }
        Log.v("알림","storageDir 존재함 " + storageDir.toString());
        imageFile = new File(storageDir,imgFileName);
        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;

    }

    private void menuOpen(){
        if(!isMenuOpen){
            fab_img.animate().translationY(-getResources().getDimension(R.dimen.add_contact));
            fab_cam.animate().translationY(-getResources().getDimension(R.dimen.read_contacts));
            isMenuOpen = true;
        } else {
            fab_img.animate().translationY(0);
            fab_cam.animate().translationY(0);

            isMenuOpen = false;
        }
    }

    private void menuClose(){
        fab_img.animate().translationY(0);
        fab_cam.animate().translationY(0);

        isMenuOpen = false;
    }


    private void tedPermission() {

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
            }
        };

        TedPermission.with(Objects.requireNonNull(mContext))
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission] ")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    class UploadFile extends AsyncTask<Object, String, String> {
        String file_name = "";
        @Override
        protected String doInBackground(Object[] params) {
            try {
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1024 * 1024;


                int pos = mCurrentPhotoPath.lastIndexOf( "/" );
                String ext = mCurrentPhotoPath.substring( pos + 1 );

                URL url = new URL("http://143.248.36.204:8080/photos/" + MainActivity.user);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Allow Inputs &amp; Outputs.
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                // Set HTTP method to POST.
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                FileInputStream fileInputStream;
                DataOutputStream outputStream;
                outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);

                outputStream.writeBytes("Content-Disposition: form-data; name=\"reference\""+ lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes("my_refrence_text");
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);

                outputStream.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\"" + ext +"\"" + lineEnd);
                outputStream.writeBytes(lineEnd);

                fileInputStream = new FileInputStream(mCurrentPhotoPath);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Read file
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                int serverResponseCode = connection.getResponseCode();
                String result = null;
                if (serverResponseCode == 200) {
                    StringBuilder s_buffer = new StringBuilder();
                    InputStream is = new BufferedInputStream(connection.getInputStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String inputLine;
                    while ((inputLine = br.readLine()) != null) {
                        s_buffer.append(inputLine);
                    }
                    result = s_buffer.toString();
                }
                fileInputStream.close();
                outputStream.flush();
                outputStream.close();
                if (result != null) {
                    Log.d("result_for upload" + ext, result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return file_name;
        }

    }




}