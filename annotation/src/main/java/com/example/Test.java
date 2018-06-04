package com.example;

/**
 * @author wangqi
 * @since 2018/6/4 17:31
 */

@Route(path = "123123")
public class Test {
    public static void main(String args[]) {

        Class clazz = Test.class;

        if (clazz.isAnnotationPresent(Route.class)) {
            Route route = (Route) clazz.getAnnotation(Route.class);
            System.out.print(route.path()+"---"+clazz.getName());

        }


    }
}
