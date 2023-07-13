package com.wp.common;

import com.wp.utils.DbUtil;
import com.wp.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ORM框架对数据库的操作
 * @author 翁鹏
 */
@Slf4j
public class DbTemplate {

    /**
     * 保存对象
     * @param dao
     * @return 返回受影响的行数
     */
    public int save(Object dao) throws SQLException {
        String tableName = AnnotationParser.getTableName(dao.getClass());
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> columnValues = new ArrayList<>();
        // 获取所有字段
        List<ColumnInfo> columnInfoList = AnnotationParser.getColumnsInfo(dao.getClass());
        for (ColumnInfo columnInfo : columnInfoList) {
            // 获取字段值
            Object fieldValue = ReflectUtil.getFieldValue(dao, columnInfo.getFieldName());
            // 字段值不为空，保存
            if (fieldValue != null) {
                columns.append(columnInfo.getColumnName()).append(",");
                values.append("?,");
                columnValues.add(fieldValue);
            }
        }
        // 删除最后一个逗号
        columns.deleteCharAt(columns.length() - 1);
        values.deleteCharAt(values.length() - 1);
        // 拼接sql
        String sql = "insert into " + tableName + "(" + columns + ") values(" + values + ")";

        // 执行sql
        return DbUtil.executeUpdate(sql, columnValues.toArray());
    }



    /**
     * 更新对象
     * @param dao
     */
    public int update(Object dao) throws SQLException {
        String tableName = AnnotationParser.getTableName(dao.getClass());
        StringBuilder columns = new StringBuilder();
        StringBuilder where = new StringBuilder();
        List<Object> columnValues = new ArrayList<>();
        List<Object> whereValues = new ArrayList<>();
        // 获取所有字段
        List<ColumnInfo> columnInfoList = AnnotationParser.getColumnsInfo(dao.getClass());
        for (ColumnInfo columnInfo : columnInfoList) {
            // 获取字段值
            Object fieldValue = ReflectUtil.getFieldValue(dao, columnInfo.getFieldName());
            // 字段值不为空，保存
            if (fieldValue != null) {
                // 判断是否是主键
                if (columnInfo.isPrimaryKey()) {
                    where.append(columnInfo.getColumnName()).append(" = ? and ");
                    whereValues.add(fieldValue);
                } else {
                    columns.append(columnInfo.getColumnName()).append(" = ?,");
                    columnValues.add(fieldValue);
                }
            }
        }
        // 判断是否有主键
        if (where.length() == 0) {
            throw new IllegalStateException("没有找到表[" + tableName + "]中的主键");
        }
        if (whereValues.isEmpty()) {
            throw new IllegalStateException("没有找到表[" + tableName + "]中的需要更新的字段");
        }
        // 删除多余的and
        where.delete(where.length() - 4, where.length());
        // 删除最后一个逗号
        columns.deleteCharAt(columns.length() - 1);

        // 拼接sql
        String sql = "update " + tableName + " set " + columns + " where " + where;
        columnValues.addAll(whereValues);
        return DbUtil.executeUpdate(sql, columnValues.toArray());


    }

    /**
     * 删除对象
     * @param dao
     * @return 返回受影响的行数
     */
    public int delete (Object dao) throws SQLException {
        // 获取表名
        String tableName = AnnotationParser.getTableName(dao.getClass());
        StringBuilder where = new StringBuilder();
        List<Object> whereValues = new ArrayList<>();
        // 获取所有字段
        List<ColumnInfo> columnInfoList = AnnotationParser.getColumnsInfo(dao.getClass());
        for (ColumnInfo columnInfo : columnInfoList) {
            // 获取字段值
            Object fieldValue = ReflectUtil.getFieldValue(dao, columnInfo.getFieldName());
            // 字段值不为空，保存
            if (fieldValue != null) {
                // 判断是否是主键
                if (columnInfo.isPrimaryKey()) {
                    where.append(columnInfo.getColumnName()).append(" = ? and ");
                    whereValues.add(fieldValue);
                }
            }
        }
        if (where.length() == 0){
            throw new IllegalStateException("没有找到表[" + tableName + "]中的主键");
        }
        if (whereValues.isEmpty()){
            throw new IllegalStateException("没有找到表[" + tableName + "]中的主键值，无法删除");
        }
        // 删除多余的and
        where.delete(where.length() - 4, where.length());
        // 拼接sql
        String sql = "delete from " + tableName + " where " + where;
        return DbUtil.executeUpdate(sql, whereValues.toArray());
    }


    /**
     * 查询所有对象
     * @param clazz
     * @return 返回对象集合
     */
    public <T> List<T> selectAll(Class<T> clazz) throws SQLException {
        // 获取表名
        String tableName = AnnotationParser.getTableName(clazz);
        // 拼接sql
        String sql = "select * from " + tableName;
        // 执行sql
        List<T> list = DbUtil.executeQuery(clazz,sql);
        // 将结果集转换为对象集合
        return list;
    }

    /**
     * 根据id查询对象
     * @param clazz
     * @param id
     * @return 返回对象
     */
    public <T> T selectById(Class<T> clazz, Object id) throws SQLException {
        // 获取表名
        String tableName = AnnotationParser.getTableName(clazz);
        // 拼接sql
        String sql = "select * from " + tableName + " where id = ?";
        // 执行sql
        List<T> list = DbUtil.executeQuery(clazz, sql, id);
        // 将结果集转换为对象集合
        return list.isEmpty() ? null : list.get(0);
    }














}
