package com.going;


import java.util.Objects;

/**
 * 通过线程本地变量存取当前使用的数据源名称
 */
public class JdbcContextHolder {
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();

    public enum JdbcType {
        READ_DS("read_ds"),
        WRITE_DS("write_ds");
        String name;

        JdbcType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static void setJdbcType(String jdbcType) {
        contextHolder.set(jdbcType);
    }

    public static void setWriteRead() {
        contextHolder.set(JdbcType.WRITE_DS.getName());
    }

    public static void reset() {
        contextHolder.set(JdbcType.WRITE_DS.getName());
    }

    public static String getJdbcType() {
        String ds = contextHolder.get();
        if(Objects.nonNull(ds)){
            return ds;
        }
        return JdbcType.WRITE_DS.getName();
    }

    /**
     * 恢复成默认的数据源，即defaultTargetDataSource，执行此方法
     */
    public static void clearJdbcType() {
        contextHolder.remove();
    }
}
