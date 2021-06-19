package com.star.aqs;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @Description: 通过反射创建一个Unsafe实例
 * @Author: zzStar
 * @Date: 06-19-2021 09:59
 */
public class UnsafeInstance {

    public static Unsafe reflectGetUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            //设置为可访问
            field.setAccessible(true);
            return (Unsafe)field.get(null);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
