package com.codelang.transform.appImp;

import com.codelang.transform.appLike.AppLike;

/**
 * @author wangqi
 * @since 2018/6/7 14:06
 */

public class AppLikeImp implements AppLike {
    @Override
    public void onCreate(String name) {
        System.out.println(name);
    }
}
