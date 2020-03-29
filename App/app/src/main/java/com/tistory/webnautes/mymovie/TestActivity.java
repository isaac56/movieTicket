package com.tistory.webnautes.mymovie;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TestActivity extends AppCompatActivity {

    EditText ip_editText;
    Button OK_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ip_editText = (EditText) findViewById(R.id.ip_editText);
        OK_button = (Button) findViewById(R.id.OK_button);

    }

    public void OK_button_clicked(View v) {

        Static.url = ip_editText.getText().toString();
        startActivity(new Intent(TestActivity.this,LoginActivity.class));
    }

}
