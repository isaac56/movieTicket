package com.tistory.webnautes.mymovie;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.tistory.webnautes.mymovie.recommendTab.RecommendActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    final String LOGIN_SUCCESS = "user.post.success";
    final String LOGIN_WRONG_PASSWORD = "user.post.fail:wrongPassword";
    final String LOGIN_NOT_EXISTS = "user.post.fail:notExists";

    EditText id_editText;
    EditText password_editText;
    Button login_button;
    Button signUp_button;

    AppCompatDialog loadingDialog;

    static LoginActivity loginActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        id_editText = findViewById(R.id.id_editText);
        password_editText = findViewById(R.id.password_editText2);
        login_button = findViewById(R.id.login_button);
        signUp_button = findViewById(R.id.signUp_button);

        loginActivity = LoginActivity.this;
    }

    public void login_button_clicked(View v) {

        if(id_editText.getText().toString().equals("")) {
            Toast.makeText(LoginActivity.this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(password_editText.getText().toString().equals("")) {
            Toast.makeText(LoginActivity.this, "패스워드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject user_info = new JSONObject();
            user_info.put("id", id_editText.getText());
            user_info.put("password", password_editText.getText());

            NetworkTask networkTask = new NetworkTask(Static.url, user_info);
            networkTask.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void signUp_button_clicked(View v){
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_slide_out_top);
        LoginActivity.this.finish();
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
                loadingDialog = new AppCompatDialog(LoginActivity.this);
                loadingDialog.setCancelable(false);
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                loadingDialog.setContentView(R.layout.dialog_loading);
                ImageView loading_icon = loadingDialog.findViewById(R.id.loading_icon_imageView);
                Animation anim = AnimationUtils.loadAnimation(LoginActivity.this,R.anim.loading);
                loading_icon.setAnimation(anim);
            }
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String result;
            HttpConnection httpConnection = new HttpConnection();
            result = httpConnection.request(url+"user/", json, "POST");

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            loadingDialog.dismiss();
            super.onPostExecute(s);
            if(s == null) {
                Toast.makeText(LoginActivity.this, "서버와 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (s) {
                case LOGIN_WRONG_PASSWORD:
                    Toast.makeText(LoginActivity.this, "패스워드가 틀립니다.", Toast.LENGTH_SHORT).show();
                    break;
                case LOGIN_NOT_EXISTS:
                    Toast.makeText(LoginActivity.this, "아이디가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                    break;
                default:

                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        Static.u_id = jsonObject.getInt("u_id");
                        Toast.makeText(LoginActivity.this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(LoginActivity.this, TabActivity.class));
                        overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_slide_out_top);
                        LoginActivity.this.finish();
                    } catch (JSONException e) {
                        Toast.makeText(LoginActivity.this, "로그인 에러.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    break;

            }
        }
    }
}