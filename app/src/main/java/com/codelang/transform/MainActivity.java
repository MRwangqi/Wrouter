package com.codelang.transform;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.codelang.api.core.WRouter;
import com.codelang.wrouter.annotation.WRoute;

@WRoute(path = "/app/main", desc = "首页")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void click(View view) {
        WRouter.getInstance().build("/app/second")
                .withInt("age", 12)
                .withString("name", "张三")
                .navigation();
    }

    public void reader(View view){
        WRouter.getInstance().build("/read/main")
                .withInt("age", 12)
                .withString("name", "张三")
                .navigation();
    }
}
