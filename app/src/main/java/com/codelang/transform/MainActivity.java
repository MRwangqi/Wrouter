package com.codelang.transform;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.codelang.api.core.WRouter;
import com.codelang.wrouter.annotation.WRoute;

@WRoute(path = "/app/main", desc = "首页")
public class MainActivity extends AppCompatActivity {

    public static final int READ_REQUEST_CODE = 0x12;

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

    public void reader(View view) {
        WRouter.getInstance().build("/read/main")
                .withInt("age", 12)
                .withString("name", "张三")
                .navigation(this, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        if (READ_REQUEST_CODE == requestCode) {
            String name = data.getStringExtra("name");

            Toast.makeText(this, "requestCode返回值=" + name, Toast.LENGTH_SHORT).show();
        }

    }
}
