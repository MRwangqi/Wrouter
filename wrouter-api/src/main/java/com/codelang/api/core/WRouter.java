package com.codelang.api.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.codelang.api.template.IRouteGroup;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author wangqi
 * @since 2018/6/5 14:59
 */

public class WRouter {

    private static Context mContext;

    private static Map<String, Class> routes = new HashMap<>();


    private WRouter() {
    }

    public static class Inner {
        private static final WRouter INSTANCE = new WRouter();
    }

    public static WRouter getInstance() {
        return Inner.INSTANCE;
    }


    public static void init(Application app) {
        mContext = app;
        initApplication();
        loadMap();
    }

    /**
     * 待插入字节码区域,插入之后会变成如下代码
     * //路由的插入
     * register(new com.codelang.wrouter.routes.Wrouter$$Group$$readcomponent());
     * register("com.codelang.wrouter.routes.Wrouter$$Group$$readcomponent");
     * //Application的插入
     * init("com.codelang.wrouter.app.Application$$readcomponent")
     */
    public static void loadMap() {
        //register("com.codelang.wrouter.routes.Wrouter$$Group$$readcomponent");
    }

    /**
     * 待插入字节码区域，application的插入
     * 就像  registerApp("com.codelang.applike.ReadApplication");
     */
    public static void initApplication() {
        //registerApp("com.codelang.applike.ReadApplication");
    }


    public static void register(String routeGroupPath) {
        Log.i("routeGroupPath", routeGroupPath);
        try {
            ((IRouteGroup) (Class.forName(routeGroupPath).getConstructor().newInstance())).loadMap(routes);
        } catch (Exception e) {
            android.util.Log.i("tag", "反射失败");
        }
    }

    public static void registerApp(String appPath) {
        Log.i("appPath", appPath);
        try {
            Class clazz = Class.forName(appPath);
            Method method = clazz.getDeclaredMethod("onCreate", Application.class);
            method.setAccessible(true);
            method.invoke(clazz.newInstance(), mContext);
        } catch (Exception e) {
            throw new RuntimeException(
                    "反射失败，可能错误原因是组件module实现的appLike对象没有实现AppLike接口");
        }
    }


    public Builder build(String path) {
        return new Builder(path);
    }


    public class Builder {
        String path;
        Bundle bundle = new Bundle();

        public Builder(String path) {
            this.path = path;
        }

        public Builder withString(String key, String value) {
            bundle.putString(key, value);
            return this;
        }

        public Builder withInt(String key, Integer anInt) {
            bundle.putInt(key, anInt);
            return this;
        }

        public void navigation() {
            navigation(null, -1);
        }


        public void navigation(Context context, final int requestCode) {
            mContext = context != null ? context : mContext;
            final Intent intent = new Intent(mContext, routes.get(path));
            intent.putExtras(bundle);
            //避免在子线程中会调用navigation操作
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (requestCode > 0) {
                        ((Activity) mContext).startActivityForResult(intent, requestCode);
                    } else {
                        mContext.startActivity(intent);
                    }
                }
            });
        }
    }

}
