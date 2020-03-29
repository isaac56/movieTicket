package com.tistory.webnautes.mymovie;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class SignUpActivity extends AppCompatActivity {

    final int DIALOG_DATE = 0;
    final String SIGN_UP_FAIL_ID_EXISTS = "user.put.fail:idExists";
    final String SIGN_UP_SUCCESS="user.put.success";

    EditText id_editText;
    EditText password_editText;
    EditText password2_editText;
    EditText name_editText;
    EditText birthDate_editText;

    AppCompatDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_sign_up);
        id_editText = findViewById(R.id.id_editText);
        password_editText = findViewById(R.id.password_editText);
        password2_editText = findViewById(R.id.password2_editText);
        name_editText = findViewById(R.id.name_editText);
        birthDate_editText = findViewById(R.id.birthDate_editText);
        birthDate_editText.setFocusable(false);
        birthDate_editText.setClickable(true);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        },600);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_slide_out_bottom);
        SignUpActivity.this.finish();
    }

    public void signUp_button_clicked(View v) {
        try {
            if(name_editText.getText().toString().equals("")) {
                Toast.makeText(SignUpActivity.this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(birthDate_editText.getText().toString().equals("")) {
                Toast.makeText(SignUpActivity.this, "생년월일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(id_editText.getText().toString().equals("")) {
                Toast.makeText(SignUpActivity.this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(password_editText.getText().toString().equals("")) {
                Toast.makeText(SignUpActivity.this, "패스워드를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!password2_editText.getText().toString().equals(password_editText.getText().toString())) {
                Toast.makeText(SignUpActivity.this, "패스워드를 확인해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject user_info = new JSONObject();
            user_info.put("id", id_editText.getText());
            user_info.put("password", password_editText.getText());
            user_info.put("name",name_editText.getText());
            user_info.put("birthDate",birthDate_editText.getText());

            NetworkTask networkTask = new NetworkTask(Static.url, user_info);
            networkTask.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void birthDate_editText_clicked(View v) {
        showDialog(DIALOG_DATE);
    }

    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case DIALOG_DATE:
                DatePickerDialog dp = new DatePickerDialog(SignUpActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        birthDate_editText.setText(i + "-" + (i1+1) + "-" + i2);
                    }
                },2000,1,1);
                return dp;
        }

        return super.onCreateDialog(id);
    }

    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private JSONObject json;

        public NetworkTask(String url, JSONObject json) {
            this.url = url;
            this.json = json;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(loadingDialog == null) {
                loadingDialog = new AppCompatDialog(SignUpActivity.this);
                loadingDialog.setCancelable(false);
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                loadingDialog.setContentView(R.layout.dialog_loading);
                ImageView loading_icon = loadingDialog.findViewById(R.id.loading_icon_imageView);
                Animation anim = AnimationUtils.loadAnimation(SignUpActivity.this,R.anim.loading);
                loading_icon.setAnimation(anim);
            }
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String result;
            HttpConnection httpConnection = new HttpConnection();
            result = httpConnection.request(url+"user/", json, "PUT");

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            loadingDialog.dismiss();
            super.onPostExecute(s);
            if(s == null) {
                Toast.makeText(SignUpActivity.this, "서버와 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (s) {
                case SIGN_UP_SUCCESS:
                    Toast.makeText(SignUpActivity.this, "회원가입에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_slide_out_bottom);
                    SignUpActivity.this.finish();
                    break;
                case SIGN_UP_FAIL_ID_EXISTS:
                    Toast.makeText(SignUpActivity.this, "해당 아이디가 존재합니다.", Toast.LENGTH_SHORT).show();
                    break;

            }

        }
    }
}