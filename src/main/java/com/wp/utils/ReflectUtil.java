package com.wp.utils;

import com.wp.common.AnnotationParser;
import com.wp.common.ColumnInfo;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 反射工具类
 */
public class ReflectUtil {

    /**
     * 获取类的字段的值
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        try {
            // 获取类
            Class<?> clazz = obj.getClass();
            // 获取字段
            Field field = clazz.getDeclaredField(fieldName);
            // 设置可访问
            field.setAccessible(true);
            // 获取字段值
            return field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 获取类的实例
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 通过表字段设置类的字段值
     */
    public static <T> void setFieldValueByColumn(T dao, String columnName, Object fieldValue) {
        try {
            // 获取类
            Class<?> clazz = dao.getClass();
            // 获取类的所有字段信息
            List<ColumnInfo> columnsInfo = AnnotationParser.getColumnsInfo(clazz);
            for (ColumnInfo columnInfo : columnsInfo) {
                // 找到对应的字段
                if (columnInfo.getColumnName().equals(columnName)) {
                    // 获取字段
                    Field field = clazz.getDeclaredField(columnInfo.getFieldName());
                    // 设置可访问
                    field.setAccessible(true);
                    // 设置字段值
                    field.set(dao, fieldValue);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
