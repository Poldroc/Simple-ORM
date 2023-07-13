package com.wp.common;



import com.wp.annotation.Column;
import com.wp.annotation.TableName;
import com.wp.utils.NameConvertUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注解解析器（可减少反射次数）
 * @author 翁鹏
 */
public class AnnotationParser {
    /**
     * 存储类名和表名的映射关系
     */
    private static Map<String, String> tableNameMap = new HashMap<>();

    /**
     * 存储类名和所有字段信息的映射关系
     */
    private static Map<String, List<ColumnInfo>> columnsInfoMap = new HashMap<>();

    /**
     * id
     */
    private static String ID = "id";


    /**
     * 获取表名
     */
    public static <T> String getTableName(Class<T> clazz) {
        String className = clazz.getName();
        String tableName = tableNameMap.get(className);
        if (tableName == null) {
            // 获取注解
            TableName tableNameAnnotation = clazz.getAnnotation(TableName.class);
            if (tableNameAnnotation != null && StringUtils.isNotBlank(tableNameAnnotation.value())) {
                tableName = tableNameAnnotation.value();
            } else {
                // 注解为空，使用驼峰类名转化为下划线命名
                tableName = NameConvertUtil.getTableName(className);
            }
            tableNameMap.put(className, tableName);
        }
        return tableName;
    }

    /**
     * 获取所有字段信息
     */
    public static <T> List<ColumnInfo> getColumnsInfo(Class<T> clazz) {
        String className = clazz.getName();
        List<ColumnInfo> columnInfoList = columnsInfoMap.get(className);
        if (columnInfoList == null) {
            columnInfoList = new ArrayList<>();
            ColumnInfo columnInfo;
            // 获取所有字段
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                // 获取字段名
                String fieldName = field.getName();
                // 获取注解
                Column columnAnnotation = field.getAnnotation(Column.class);
                if (columnAnnotation != null && StringUtils.isNotBlank(columnAnnotation.value())) {
                    // 判断是否为主键
                    if (columnAnnotation.isPrimaryKey()||ID.equals(columnAnnotation.value())) {
                        columnInfo = new ColumnInfo();
                        columnInfo.setTableName(getTableName(clazz));
                        columnInfo.setFieldName(fieldName);
                        columnInfo.setColumnName(columnAnnotation.value());
                        columnInfo.setPrimaryKey(true);
                        columnInfoList.add(columnInfo);
                    }
                    // 注解不为空，使用注解的值作为表字段名
                    columnInfo = new ColumnInfo();
                    columnInfo.setTableName(getTableName(clazz));
                    columnInfo.setFieldName(fieldName);
                    columnInfo.setColumnName(columnAnnotation.value());
                    columnInfoList.add(columnInfo);
                } else {
                    columnInfo = new ColumnInfo();
                    if (ID.equals(fieldName)){
                        columnInfo.setPrimaryKey(true);
                    }
                    // 注解为空，使用驼峰字段名转化为下划线命名
                    columnInfo.setTableName(getTableName(clazz));
                    columnInfo.setFieldName(fieldName);
                    columnInfo.setColumnName(NameConvertUtil.camelToUnderline(fieldName));
                    columnInfoList.add(columnInfo);
                }
            }
            columnsInfoMap.put(className, columnInfoList);
        }
        return columnInfoList;
    }

}
