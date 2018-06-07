package com.codelang.applike;

import android.app.Application;
import android.util.Log;

import com.codelang.api.template.AppLike;

/**
 * @author wangqi
 * @since 2018/6/7 13:47
 */

public class ReadApplication implements AppLike {
    @Override
    public void onCreate(Application app) {
        Log.i("hahah", app.toString());

    }
}
