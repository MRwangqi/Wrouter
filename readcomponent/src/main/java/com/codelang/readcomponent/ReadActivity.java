package com.codelang.readcomponent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.codelang.wrouter.annotation.WRoute;

@WRoute(path = "/read/main", desc = "读书")
public class ReadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
    }
}
