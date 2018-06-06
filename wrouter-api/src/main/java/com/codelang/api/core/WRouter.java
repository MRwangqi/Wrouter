package com.codelang.api.core;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.codelang.api.template.IRouteGroup;

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

    public static void init(Application app) {
        mContext = app;

//        try {
//            //com.codelang.wrouter.routes.Wrouter$$Group$$readcomponent
//            String className = "com.codelang.wrouter.routes.Wrouter$$Group$$readcomponent";
//            ((IRouteGroup) (Class.forName(className).getConstructor().newInstance())).loadMap(routes);
//
//        } catch (Exception e) {
//            android.util.Log.i("tag", "反射失败");
//        }

        loadMap();
    }


    public static void loadMap() {
        //待插入字节码区域,插入之后会变成如下代码
//        register(new com.codelang.wrouter.routes.Wrouter$$Group$$readcomponent());
//        register(new com.codelang.wrouter.routes.Wrouter$$Group$$app());

//        register("com.codelang.wrouter.routes.Wrouter$$Group$$readcomponent");
    }


    public static void register(String routeGroupPath) {
            Log.i("routeGroupPath", routeGroupPath);
            try {
                ((IRouteGroup) (Class.forName(routeGroupPath).getConstructor().newInstance())).loadMap(routes);
            } catch (Exception e) {
                android.util.Log.i("tag", "反射失败");
            }
    }


    public static class Inner {
        private static final WRouter INSTANCE = new WRouter();
    }

    public static WRouter getInstance() {
        return Inner.INSTANCE;
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
            Intent intent = new Intent(mContext, routes.get(path));
            intent.putExtras(bundle);
            mContext.startActivity(intent);
        }
    }

}
