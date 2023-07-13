package com.wp.common;

import lombok.Data;

/**
 * 字段信息,用来封装字段名和字段值，以及是否为主键，用于保存和更新操作，以及查询操作的结果封装，方便后续的ORM操作，比如将查询结果封装成对象，或者将对象封装成sql语句，或者将对象封装成sql语句的参数
 * @author 翁鹏
 */
@Data
public class ColumnInfo {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 类字段名
     */
    private String fieldName;

    /**
     * 表字段名
     */
    private String columnName;

    /**
     * 是否为主键
     */
    private boolean isPrimaryKey;



}
