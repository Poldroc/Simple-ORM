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
