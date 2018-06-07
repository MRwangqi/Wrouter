package com.codelang.readcomponent;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.codelang.wrouter.annotation.WRoute;

@WRoute(path = "/read/main", desc = "读书")
public class ReadActivity extends AppCompatActivity {

    public static final int READ_REQUEST_CODE = 0x12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
    }


    public void back(View view) {

        Intent intent = getIntent();
        intent.putExtra("name", "张三");
        setResult(READ_REQUEST_CODE, intent);
        finish();
    }
}
