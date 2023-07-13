# ORM框架

==本文档仅作为学习过程记录，若有错误之处欢迎提出pr/issue==

## 一、什么是ORM框架

> **对象关系映射（Object Relation Mapping，简称ORM，或O/RM，或O/R mapping），指的是将一个Java中的对象与关系型数据库中的表建立一种映射关系，从而操作对象就可以操作数据库中的表。**

所谓的ORM就是利用描述对象和数据库表之间映射的元数据，自动把Java应用程序中的对象，持久化到关系型数据库的表中。**Hibernate与MyBatis都是流行的持久层开发框架**。



## 二、一个简单的orm框架

### 1、所需知识

1. Java编程语言：熟悉Java的语法、面向对象编程的概念和特性，以及Java的相关类库和框架。
2. 数据库知识：了解关系型数据库（如MySQL、Oracle等）的基本概念，包括表、字段、主键、外键、索引等。
3. SQL语言：熟悉SQL语言，能够编写SQL查询、插入、更新和删除等操作。
4. JDBC（Java Database Connectivity）：掌握JDBC的使用，包括连接数据库、执行SQL语句、处理结果集等。
5. 反射（Reflection）：了解Java的反射机制，可以在运行时动态获取和操作类的属性和方法。
6. 设计模式：熟悉常用的设计模式，如工厂模式、单例模式、代理模式等，可以应用到ORM框架的设计中。
7. 事务处理：了解事务的概念和特性，掌握如何使用JDBC的事务管理功能。
8. XML或注解处理：考虑使用XML配置文件或注解来定义实体类与数据库表之间的映射关系，需要掌握相关技术，如DOM解析、SAX解析、Java注解等。
9. 并发控制：在多线程环境下使用ORM框架时，需要考虑并发控制，例如使用锁或其他机制来保证数据的一致性和并发安全性。
10. 性能优化：了解数据库查询优化的基本原则和技巧，可以对ORM框架进行性能优化，提高查询效率和系统响应速度。
11. 测试和调试：熟悉单元测试和集成测试的方法和工具，能够对ORM框架进行测试和调试，确保其功能的正确性和稳定性。



### 2、原理

在使用ORM框架时，我们可以像操作对象一样操作数据的存储，这是通过ORM框架的对象关系映射功能实现的。通过在对象和数据库表之间建立映射关系，将对象的属性映射到数据库表的列上。这样，在进行对象操作时，ORM框架会根据映射关系自动转换操作为对应的SQL语句，从而实现数据的存储和检索。

> ORM : 对象操作 ===> 对应的SQL语句

让我们来看看一句简单的sql

```sql
INSERT INTO user (name, email) VALUES ('WengPeng', 'engroc@foxmail.com');
INSERT INTO tablename (column1, column2) VALUES (value1, value2)
```

综上所述，如果我们能从`对象(bean)`中得到 `表名(tablename)` `列名(column)` `值(value)`，我们也可以编写一个简单的ORM框架！





### 3、实现

##### 实现一个简单的orm框架

该orm框架具备以下功能：

- 能进行表的结构映射
- 实现简单的增删改查等等api接口
- 支持事务



**具体实现如下：**

本项目使用 Druid 数据库连接池作为连接管理工具

#### druid.properties

```properties
url=jdbc:mysql://localhost:3306/orm_db?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true
driverClassName=com.mysql.cj.jdbc.Driver
username=root
password=123456

#连接池初始化时创建的连接数
initialSize=10
#连接池中最大的活动连接数
maxActive=30
#连接池中保持的最小空闲连接数
minIdle=10
#获取连接时的最大等待时间，单位为毫秒
maxWait=2000
#是否缓存预编译语句
poolPreparedStatements=true
#连接池中可以缓存的最大预编译语句数
maxOpenPreparedStatements=20
```

#### 3.1、封装数据库工具类

```java
package com.wp.utils;
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

```



#### 3.2、使用注解或者下划线驼峰命名转换来将 Bean 和数据库字段关联

**`若有注解则使用注解来将Bean 和数据库字段关联、若无则将来将 Bean  的驼峰命名的字段转化为下划线命名来与数据库字段关联`**

**驼峰、下划线命名转换工具类如下：**

```java
package com.wp.utils;
/**
 * 驼峰、下划线命名转换工具类
 * @author 翁鹏
 */
public class NameConvertUtil {

    /**
     * 下划线
     */

    private static final char UNDERLINE = '_';

    /**
     * 下划线命名转驼峰命名
     * @param name 下划线命名字符串
     * @return 驼峰命名字符串
     */
    public static String underlineToCamel(String name) {
        // 用下划线将字符串分割
        String[] words = name.split(String.valueOf(UNDERLINE));
        StringBuilder sb = new StringBuilder();
        // 遍历分割后的字符串数组
        for (String word : words) {
            // 将首字母大写
            sb.append(word.substring(0, 1).toUpperCase());
            // 将剩余部分拼接
            sb.append(word.substring(1));
        }
        return sb.toString();
    }

    /**
     * 驼峰命名转下划线命名
     * @param name 驼峰命名字符串
     * @return 下划线命名字符串
     */
    public static String camelToUnderline(String name) {
        StringBuilder sb = new StringBuilder();
        // 遍历字符串
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            // 判断是否为大写字母
            if (Character.isUpperCase(c)) {
                // 将大写字母转换为小写字母
                c = Character.toLowerCase(c);
                // 在大写字母前添加下划线
                sb.append(UNDERLINE);
            }
            // 拼接字符
            sb.append(c);
        }
        // 开头为下划线的情况
        if (sb.charAt(0) == UNDERLINE) {
            // 删除开头的下划线
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    /**
     * 根据类名获取表名
     */
    public static String getTableName(String className) {
        // 获取类名
        String[] names = className.split("\\.");
        String name = names[names.length - 1];
        // 将类名转换为下划线命名
        return NameConvertUtil.camelToUnderline(name);
    }
}

```

**定义的注解如下：**

| 注解       | 作用                                                   | 说明             |
| ---------- | ------------------------------------------------------ | ---------------- |
| @Column    | 为类字段起别名，对应数据库表中的字段名；明确是否为主键 | 标注在类的字段上 |
| @TableName | 为表起别名，对应数据库表名                             | 标注在类名上     |

```java
package com.wp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 翁鹏
 * 表名注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableName {
    /**
     * 表名
     */
    String value();
}

```

```java
package com.wp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 翁鹏
 * 字段注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * 字段名
     */
    String value();

    /**
     * 是否为主键
     */
    boolean isPrimaryKey() default false;
}
```

**封装注解解析类，实现类名与表名、类字段名与表字段名关联，具体如下：**

```java
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

```

**让我们来分析上述代码 : **

* 定义了两个HashMap存储映射关系，可减少反射次数，提高效率

```java
    /**
     * 存储类名和表名的映射关系
     */
    private static Map<String, String> tableNameMap = new HashMap<>();

    /**
     * 存储类名和所有字段信息的映射关系
     */
    private static Map<String, List<ColumnInfo>> columnsInfoMap = new HashMap<>();
```

* *（getTableName）*获取表名的具体实现：

  ```java
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
  ```

  1. 获取传入实体类的全类名 `className`，通过 `clazz.getName()` 方法获取。
  2. 利用 `className` 作为键从 `tableNameMap` 中获取对应的表名 `tableName`。
  3. 如果 `tableName` 为 `null`，则表示该实体类的表名尚未缓存，需要进行进一步处理。
  4. **使用反射获取 `clazz` 上的 `TableName` 注解对象 `tableNameAnnotation`。**
  5. **判断 `tableNameAnnotation` 是否为空且注解的值 `value` 不为空，如果满足条件，则将 `value` 赋给 `tableName`。**
  6. **如果 `tableNameAnnotation` 为空或注解的值为空，说明未使用 `TableName` 注解，则根据驼峰命名规则将 `className` 转化为下划线命名的表名。**
  7. 将生成的 `tableName` 放入 `tableNameMap` 中，以便下次获取时直接从缓存中取值。
  8. 返回最终的 `tableName`。

  需要注意的是，该实现中使用了 `StringUtils.isNotBlank()` 方法来判断字符串是否非空非null，需要确保项目中引入了相关的字符串处理工具类，如 Apache Commons Lang 等。

  此方法的目的是为了获取实体类对应的表名，以便在进行数据库操作时使用正确的表名。根据实际需求，你可以根据自己的命名规则和业务规范对表名进行处理和映射，以适应具体的项目需求。

* *（getColumnsInfo）*获取所有字段信息的具体实现：

  ```java
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
  ```

  首先我们需要封装一个字段信息类*ColumnInfo*，**用来封装字段名和字段值，以及是否为主键**，用于保存和更新操作，以及查询操作的结果封装，方便后续的ORM操作，比如将查询结果封装成对象，或者将对象封装成sql语句，或者将对象封装成sql语句的参数

  ```java
  package com.wp.common;
  
  import lombok.Data;
  
  /**
   * 字段信息
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
  
  ```

  接着就是*getColumnsInfo*方法的具体实现：

  1. 获取传入实体类的全类名 `className`，通过 `clazz.getName()` 方法获取。
  2. 利用 `className` 作为键从 `columnsInfoMap` 中获取对应的字段信息列表 `columnInfoList`。
  3. 如果 `columnInfoList` 为 `null`，则表示该实体类的字段信息尚未缓存，需要进行进一步处理。
  4. 创建一个空的 `columnInfoList` 列表，用于存储字段信息。
  5. 遍历实体类的所有字段，通过 `clazz.getDeclaredFields()` 方法获取。
  6. **对于每个字段，获取字段名 `fieldName`。**
  7. **使用反射获取字段上的 `Column` 注解对象 `columnAnnotation`。**
  8. **判断 `columnAnnotation` 是否为空且注解的值 `value` 不为空。如果满足条件，说明该字段使用了 `Column` 注解。**
  9. 判断 `columnAnnotation` 是否为主键字段，通过判断 `columnAnnotation.isPrimaryKey()` 方法返回值或者判断字段名是否为 `"ID"`。
  10. 根据注解的值创建一个 `ColumnInfo` 对象，并设置表名、字段名、列名和是否为主键，将其添加到 `columnInfoList` 列表中。
  11. **如果注解为空或注解的值为空，说明该字段未使用 `Column` 注解，就使用驼峰字段名转化为下划线命名作为表字段。**
  12. 创建一个 `ColumnInfo` 对象，并根据字段名生成列名，同时设置表名、字段名、列名和是否为主键，将其添加到 `columnInfoList` 列表中。
  13. 将生成的 `columnInfoList` 放入 `columnsInfoMap` 中，以便下次获取时直接从缓存中取值。
  14. 返回最终的 `columnInfoList`。



将 Bean 和数据库字段关联后，实现我们ORM框架的增删查改就更进一步啦！

#### 3.3、ORM框架对数据库的操作

**增删查改离不开数据处理，于是我们要用反射实现一个工具类*ReflectUtil* 来获取字段值、创建类实例和通过表字段设置类字段值。**

```java
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
```

**结合反射实现ORM的增删查改**

> 增

```java
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
```



> 删

```java
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
```



> 查

```java
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

```



> 改

```java
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
```

**通过上面的示例，就可以简单的实现一个 ORM。**

### 4、测试

```java
import com.wp.common.DbTemplate;
import dao.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import java.util.List;
@Slf4j
public class test {

    @Test
    public void testSave() {
        User user = new User();
        user.setId(222);
        user.setName("翁");
        user.setPassword("12345");
        user.setPhoneNumber("1234589");
        user.setIsDelete(0);
        System.out.println(user);
        DbTemplate dbTemplate = new DbTemplate();
        try {
            dbTemplate.save(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testUpdate() {
        User user = new User();
        user.setId(222);
        user.setName("张");
        DbTemplate dbTemplate = new DbTemplate();
        try {
            dbTemplate.update(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDelete() {
        User user = new User();
        user.setId(222);
        DbTemplate dbTemplate = new DbTemplate();
        try {
            dbTemplate.delete(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSelectAll() {
        User user = new User();
        DbTemplate dbTemplate = new DbTemplate();
        try {
            List<? extends User> users = dbTemplate.selectAll(user.getClass());
            for (User user1 : users) {
                log.info(user1.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSelectById() {
        User user = new User();
        DbTemplate dbTemplate = new DbTemplate();
        try {
            User user1 = dbTemplate.selectById(user.getClass(), 222);
            log.info(user1.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

```



