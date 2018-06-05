package com.codelang.transform;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.codelang.api.template.IRouteGroup;
import com.codelang.wrouter.annotation.Autowired;
import com.codelang.wrouter.annotation.WRoute;
import com.codelang.wrouter.routes.Wrouter$$Group$$app;

import java.util.HashMap;
import java.util.Map;

@WRoute(path = "/app/main", desc = "首页")
public class MainActivity extends AppCompatActivity {


    @Autowired
    String name;

    @Autowired
    int age;


    Map<String, Class> routes = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            String className = "com.codelang.wrouter.routes.Wrouter$$Group$$app";
            ((IRouteGroup) (Class.forName(className).getConstructor().newInstance())).loadMap(routes);

        } catch (Exception e) {
            Log.i("tag", "反射失败");
        }

    }

    public void click(View view) {
        Intent intent = new Intent(this, routes.get("/app/second"));
        startActivity(intent);
    }
}
