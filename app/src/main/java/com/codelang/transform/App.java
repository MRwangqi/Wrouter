package com.codelang.transform;

import android.app.Application;

import com.codelang.api.core.WRouter;

/**
 * @author wangqi
 * @since 2018/6/5 17:02
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        WRouter.init(this);
    }

}
