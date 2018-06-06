package com.codelang.transform;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.codelang.wrouter.annotation.WRoute;

@WRoute(path = "/app/second", desc = "第二个页面")
public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        int age=getIntent().getIntExtra("age",0);
        String name=getIntent().getStringExtra("name");

       TextView tx= findViewById(R.id.content);
        tx.setText("age="+age+"\nname="+name);
    }
}
