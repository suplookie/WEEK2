package com.example.week2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.week2.Retrofit.IMyService;
import com.example.week2.Retrofit.LoginInfo;
import com.example.week2.Retrofit.RetrofitClient;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.rengwuxian.materialedittext.MaterialEditText;


import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {


    TextView txt_create_account, txt_delete_account;
    MaterialEditText edt_login_email, edtlogin_password;
    Button login_btn, facebook_login;

    static CompositeDisposable compositeDisposable = new CompositeDisposable();
    static IMyService iMyService;
    String user;

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init Service
        Retrofit retrofitClient = RetrofitClient.getInstance();
        iMyService = retrofitClient.create(IMyService.class);

        //Init view
        edt_login_email = findViewById(R.id.edit_email);
        edtlogin_password = findViewById(R.id.edit_password);
        login_btn = findViewById(R.id.button_login);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loginUser(edt_login_email.getText().toString(), edtlogin_password.getText().toString()) != null) {
                    Toast.makeText(MainActivity.this, "hello " + user, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, TabActivity.class);
                    startActivity(intent);
                }
            }
        });

        facebook_login = findViewById(R.id.facebook);
        facebook_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Service in preparation", Toast.LENGTH_SHORT).show();
            }
        });

        txt_create_account = findViewById(R.id.txt_create_account);
        txt_create_account.setOnClickListener(new View.OnClickListener() {
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
                                MaterialEditText edt_register_email = register_layout.findViewById(R.id.edit_email);
                                MaterialEditText edt_register_password = register_layout.findViewById(R.id.edit_password);
                                MaterialEditText edt_register_name = register_layout.findViewById(R.id. edit_name);

                                if (TextUtils.isEmpty(edt_register_email.getText().toString())) {
                                    Toast.makeText(MainActivity.this, "Email cannot be null or empty", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (TextUtils.isEmpty(edt_register_password.getText().toString())) {
                                    Toast.makeText(MainActivity.this, "Password cannot be null or empty", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (TextUtils.isEmpty(edt_register_name.getText().toString())) {
                                    Toast.makeText(MainActivity.this, "Name cannot be null or empty", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                registerUser(edt_register_email.getText().toString(), edt_register_name.getText().toString(), edt_register_password.getText().toString());


                            }
                        }).show();
            }
        });

        txt_delete_account = findViewById(R.id.txt_delete_account);
        txt_delete_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View register_layout = LayoutInflater.from(MainActivity.this).inflate(R.layout.delete_layout, null);
                new MaterialStyledDialog.Builder(MainActivity.this)
                        .setTitle("DELETE ACCOUNT")
                        .setDescription("Please fill out all fields")
                        .setCustomView(register_layout).setNegativeText("CANCEL")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveText("DELETE")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                MaterialEditText edt_register_email = register_layout.findViewById(R.id.edit_email);
                                MaterialEditText edt_register_password = register_layout.findViewById(R.id.edit_password);

                                if (TextUtils.isEmpty(edt_register_email.getText().toString())) {
                                    Toast.makeText(MainActivity.this, "Email cannot be null or empty", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (TextUtils.isEmpty(edt_register_password.getText().toString())) {
                                    Toast.makeText(MainActivity.this, "Password cannot be null or empty", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                deleteUser(edt_register_email.getText().toString(), edt_register_password.getText().toString());
                                //Toast.makeText(Login.this, "deletion not implemented yet", Toast.LENGTH_SHORT).show();


                            }
                        }).show();
            }
        });


    }

    private void registerUser(String email, String name, String password) {
        compositeDisposable.add(iMyService.registerUser(email, name, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        Toast.makeText(MainActivity.this, ""+response, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void deleteUser(String email, String password) {
        compositeDisposable.delete(iMyService.deleteUser(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        Toast.makeText(MainActivity.this, ""+response, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private String  loginUser(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email cannot be null or empty", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password cannot be null or empty", Toast.LENGTH_SHORT).show();
            return null;
        }


        compositeDisposable.add(iMyService.loginUser(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        //Toast.makeText(MainActivity.this, ""+response, Toast.LENGTH_SHORT).show();
                        user = response;
                        if (("" + response).equals("\"Wrong password\"")) user = null;
                    }
                }));
        return user;

    }
    final boolean[] result = {false};

}
