package com.wp.utils;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


/**
 * 数据库工具类 使用 Durid 连接池
 * 1. 获取数据库连接
 * 2. 释放数据库连接
 * 3. 执行数据库操作
 * 4. 释放数据库资源
 * 5. 开启事务
 * 6. 提交事务
 * 7. 回滚事务
 * @author 翁鹏
 */
@Slf4j
public class DbUtil {

    // 获得数据源的配置文件
    private static Properties properties = new Properties();
    private static DataSource dataSource = null;


    // 静态代码块，加载配置文件
    static {
        try {
            properties.load(DbUtil.class.getClassLoader().getResourceAsStream("druid.properties"));
            dataSource = DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 关闭数据库连接
     */
    public static void closeConnection(Connection connection,PreparedStatement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            log.info("关闭数据库连接失败");
            e.printStackTrace();
        }
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            log.info("关闭数据库连接失败");
            e.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            log.info("关闭数据库连接失败");
            e.printStackTrace();
        }
    }

    /**
     * 执行查询操作
     */
    public static <T>  List<T> executeQuery(Class<T> clazz,String sql , Object... params) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<T> list = new ArrayList<>();
        try {
            connection = getConnection();
            // 开启事务
            connection.setAutoCommit(false);

            statement = connection.prepareStatement(sql);

            if (params != null) {
                // 设置查询参数
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
            }
            resultSet = statement.executeQuery();
            connection.commit(); // 提交事务

            // 将将ResultSet转换为List<T> ，因为connection关闭后，resultSet也会关闭，所以在这里转换
            list = resultSetToList(clazz, resultSet);

            log.info("【 执行sql ："+ sql+" 】");
            log.info("【 params ："+ Arrays.toString(params) +" 】");
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback(); // 回滚事务
            }
            throw e;
        } finally {
            if (connection != null) {
                // 恢复自动提交
                connection.setAutoCommit(true);
            }
            closeConnection(connection, statement, null);
        }
        return list;

    }

    /**
     * 执行更新操作
     */
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        int rowsAffected = 0;

        try {
            connection = getConnection();
            // 开启事务
            connection.setAutoCommit(false);

            statement = connection.prepareStatement(sql);

            // 设置更新参数
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            rowsAffected = statement.executeUpdate();
            connection.commit(); // 提交事务
            log.info("【 执行sql ："+ sql+" 】");
            log.info("【 params ："+ Arrays.toString(params) +" 】");
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback(); // 回滚事务
            }
            throw e;
        } finally {
            if (connection != null) {
                // 恢复自动提交
                connection.setAutoCommit(true);
            }
            closeConnection(connection, statement, null);
        }

        return rowsAffected;
    }

    /**
     * 将ResultSet转换为List<T>
     */
    public static <T> List<T> resultSetToList(Class<T> clazz, ResultSet resultSet) throws SQLException {
        List<T> list = new ArrayList<>();
        try {
            // 获取ResultSet的列数
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                T t = ReflectUtil.newInstance(clazz);
                for (int i = 0; i < columnCount; i++) {
                    String columnName = metaData.getColumnName(i + 1);
                    Object columnValue = resultSet.getObject(i + 1);
                    ReflectUtil.setFieldValueByColumn(t, columnName, columnValue);
                }
                list.add(t);
            }
        } catch (SQLException e) {
            throw e;
        }
        return list;
    }

}
