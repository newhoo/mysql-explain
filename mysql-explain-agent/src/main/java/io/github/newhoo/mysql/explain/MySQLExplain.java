package io.github.newhoo.mysql.explain;

import io.github.newhoo.mysql.common.Config;
import io.github.newhoo.mysql.common.Constant;
import io.github.newhoo.mysql.common.Log;
import io.github.newhoo.mysql.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MySQL执行计划
 *
 * @author zunrong
 */
public final class MySQLExplain {

    /**
     * 调用：MySQLExplainCBP#process
     *
     * @param conn java.sql.Connection
     * @param sql  sql string
     */
    public static void explainSql(Object conn, String sql) {
        if (conn == null || sql == null) {
            return;
        }
        // 打印元信息
        printConnectMetaData(conn);

        Log.debug("before explain: %s, %s", conn, sql);
        // 2023-08-23 8.x 特殊处理，使用toString()获取
        if (sql.startsWith("com.mysql.cj.jdbc.ClientPreparedStatement: ")) {
            sql = sql.replace("com.mysql.cj.jdbc.ClientPreparedStatement: ", "");
        }

        // 打印SQL
        boolean printSQL = false;
        if (Config.showSQL && !StringUtils.containsAny(sql, Config.showSQLFilterKeywords)) {
            printSQL = true;
            ExplainHelper.printSQL(sql, Config.showSQL);
        }

        try {
            // 前置处理：条件过滤
            if (!processBefore(sql)) {
                return;
            }

            // 通过反射操作，支持tomcat应用运行
            Method Connection_createStatement = conn.getClass().getMethod("createStatement");
            Method Statement_executeQuery = Connection_createStatement.getReturnType().getMethod("executeQuery", String.class);
            Method Statement_close = Connection_createStatement.getReturnType().getMethod("close");

            Method ResultSet_next = Statement_executeQuery.getReturnType().getMethod("next");
            Method ResultSet_getString_Int = Statement_executeQuery.getReturnType().getMethod("getString", int.class);
            Method ResultSet_close = Statement_executeQuery.getReturnType().getMethod("close");
            Method ResultSet_getMetaData = Statement_executeQuery.getReturnType().getMethod("getMetaData");
            Method ResultSetMetaData_getColumnCount = ResultSet_getMetaData.getReturnType().getMethod("getColumnCount");
            Method ResultSetMetaData_getColumnName = ResultSet_getMetaData.getReturnType().getMethod("getColumnName", int.class);

            Object stmt = Connection_createStatement.invoke(conn);
            Object resultSet = Statement_executeQuery.invoke(stmt, (Config.useTraditionalFormat ? "EXPLAIN FORMAT=TRADITIONAL " : "EXPLAIN ") + sql);
            Object resultSetMetaData = ResultSet_getMetaData.invoke(resultSet);
            int columnCount = (int) ResultSetMetaData_getColumnCount.invoke(resultSetMetaData);

            // 头
            List<String> headerColumns = Stream.iterate(1, i -> i + 1)
                                               .limit(columnCount)
                                               .map(i -> {
                                                   try {
                                                       return (String) ResultSetMetaData_getColumnName.invoke(resultSetMetaData, i);
                                                   } catch (Exception e) {
                                                       return "error: " + e;
                                                   }
                                               })
                                               .collect(Collectors.toList());
            // 值
            List<List<String>> valueColumnsList = new ArrayList<>();
            while ((boolean) ResultSet_next.invoke(resultSet)) {
                List<String> valueColumnList = Stream.iterate(1, i -> i + 1)
                                                     .limit(columnCount)
                                                     .map(i -> {
                                                         try {
                                                             return (String) ResultSet_getString_Int.invoke(resultSet, i);
                                                         } catch (Exception e) {
                                                             return "error: " + e;
                                                         }
                                                     })
                                                     .map(s -> StringUtils.defaultIfEmpty(s, "NULL"))
                                                     .collect(Collectors.toList());
                valueColumnsList.add(valueColumnList);
            }
            ResultSet_close.invoke(resultSet);
            Statement_close.invoke(stmt);

            ExplainResultVo explainResult = new ExplainResultVo(headerColumns, valueColumnsList);
            Log.debug("explainResult: %s", explainResult);

            // 打印结果
            analyzeResult(sql, explainResult, printSQL);
        } catch (Exception se) {
            Log.error(se, "exception: %s\n[%s]", se.toString(), sql);
        }
    }

    private static boolean printConnectMetaData = true;

    /**
     * 打印元信息
     */
    private static void printConnectMetaData(Object conn) {
        if (printConnectMetaData) {
            printConnectMetaData = false;
            try {
                Method Connection_getMetaData = conn.getClass().getMethod("getMetaData");
                Object connectionMetaData = Connection_getMetaData.invoke(conn);
                {
                    Method DatabaseMetaData_getDatabaseProductName = Connection_getMetaData.getReturnType().getMethod("getDatabaseProductName");
                    Method DatabaseMetaData_getDatabaseProductVersion = Connection_getMetaData.getReturnType().getMethod("getDatabaseProductVersion");
                    Method DatabaseMetaData_getDriverName = Connection_getMetaData.getReturnType().getMethod("getDriverName");
                    Method DatabaseMetaData_getDriverVersion = Connection_getMetaData.getReturnType().getMethod("getDriverVersion");
                    Log.info("connection metadata(version): %s(%s) -- %s(%s)",
                            DatabaseMetaData_getDatabaseProductName.invoke(connectionMetaData),
                            DatabaseMetaData_getDatabaseProductVersion.invoke(connectionMetaData),
                            DatabaseMetaData_getDriverName.invoke(connectionMetaData),
                            DatabaseMetaData_getDriverVersion.invoke(connectionMetaData)
                    );
                }
                if (Config.isDebug) {
                    Method DatabaseMetaData_getUserName = Connection_getMetaData.getReturnType().getMethod("getUserName");
                    Method DatabaseMetaData_getURL = Connection_getMetaData.getReturnType().getMethod("getURL");
                    Log.info("connection metadata(URL): %s -- %s",
                            DatabaseMetaData_getURL.invoke(connectionMetaData),
                            DatabaseMetaData_getUserName.invoke(connectionMetaData)
                    );
                }
            } catch (Exception e) {
                Log.error(e, "print connection metadata exception: %s", e.toString());
            }
        }
    }

    /**
     * 前置检查，检查是否执行计划
     */
    private static boolean processBefore(String sql) {
        String[] s = sql.substring(0, 10).split(" ", 2);
        if (StringUtils.isEmptyArray(s)) {
            Log.debug("unsupported explain: sql.split(\" \", 2) return empty");
            return false;
        }
        if (Arrays.asList(Config.filterSqlKeywords).contains("*") || StringUtils.containsAny(sql, Config.filterSqlKeywords)) {
            Log.debug("unsupported explain - filter out by [before] keywords: %s", Arrays.asList(Config.filterSqlKeywords));
            return false;
        }
        if (!Constant.SUPPORTED_EXPLAIN_SQL.contains(s[0])) {
            // select*,id from t_api;
            if (!s[0].startsWith("SELECT*") && !s[0].startsWith("select*")) {
                Log.debug("unsupported explain - start word not in: %s, %s", s[0], Constant.SUPPORTED_EXPLAIN_SQL);
                return false;
            }
        }
        return true;
    }

    /**
     * 分析结果
     */
    private static void analyzeResult(String sql, ExplainResultVo explainResult, boolean printSQL) {
        boolean needPrint = false;

        // * 打印所有结果
        if (Arrays.asList(Config.typeOptimizationItems).contains("*") || Arrays.asList(Config.extraOptimizationItems).contains("*")) {
            needPrint = true;
        } else {
            // 找到 type 、Extra 列位置
            int typeIndex = -1;
            int extraIndex = -1;
            if (explainResult.getHeaderColumns().size() >= 12
                    && "type".equals(explainResult.getHeaderColumns().get(4))
                    && "Extra".equals(explainResult.getHeaderColumns().get(11))) {
                typeIndex = 4;
                extraIndex = 11;
            } else {
                for (int i = 0; i < explainResult.getHeaderColumns().size(); i++) {
                    if ("type".equals(explainResult.getHeaderColumns().get(i))) {
                        typeIndex = i;
                        if (extraIndex > -1) {
                            break;
                        }
                    }
                    if ("Extra".equals(explainResult.getHeaderColumns().get(i))) {
                        extraIndex = i;
                        if (typeIndex > -1) {
                            break;
                        }
                    }
                }
            }
            Log.debug("type index: %s, Extra index: %s", typeIndex, extraIndex);
            for (List<String> valueColumns : explainResult.getValueColumnsList()) {
                if (typeIndex > -1 && StringUtils.containsAny(valueColumns.get(typeIndex), Config.typeOptimizationItems)) {
                    needPrint = true;
                    break;
                }
                if (extraIndex > -1 && StringUtils.containsAny(valueColumns.get(extraIndex), Config.extraOptimizationItems)) {
                    needPrint = true;
                    break;
                }
            }
        }

        if (needPrint) {
            ExplainHelper.printExplainResult(sql, explainResult, printSQL);
        }
    }
}