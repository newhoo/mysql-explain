package io.github.newhoo.mysql.explain;

import io.github.newhoo.mysql.common.Config;
import io.github.newhoo.mysql.common.Constant;
import io.github.newhoo.mysql.common.Log;
import io.github.newhoo.mysql.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            // 前置处理：条件过滤，打印日志等
            if (!processBefore(sql)) {
                return;
            }

            Method Connection_createStatement = conn.getClass().getMethod("createStatement");
            Method Statement_executeQuery = Connection_createStatement.getReturnType().getMethod("executeQuery", String.class);
            Method Statement_close = Connection_createStatement.getReturnType().getMethod("close");
            Method ResultSet_next = Statement_executeQuery.getReturnType().getMethod("next");
            Method ResultSet_getString = Statement_executeQuery.getReturnType().getMethod("getString", String.class);
            Method ResultSet_close = Statement_executeQuery.getReturnType().getMethod("close");

            Object stmt = Connection_createStatement.invoke(conn);
            Object resultSet = Statement_executeQuery.invoke(stmt, "EXPLAIN " + sql);

            List<ExplainResultVo> explainResultList = new ArrayList<>();
            while ((boolean) ResultSet_next.invoke(resultSet)) {
                ExplainResultVo explainResultVo = new ExplainResultVo();
                explainResultVo.setId((String) ResultSet_getString.invoke(resultSet, "id"));
                explainResultVo.setSelectType((String) ResultSet_getString.invoke(resultSet, "select_type"));
                explainResultVo.setTable((String) ResultSet_getString.invoke(resultSet, "table"));
                try {
                    explainResultVo.setPartitions((String) ResultSet_getString.invoke(resultSet, "partitions"));
                    explainResultVo.setFiltered((String) ResultSet_getString.invoke(resultSet, "filtered"));
                } catch (Exception ignored) {
                }
                explainResultVo.setType((String) ResultSet_getString.invoke(resultSet, "type"));
                explainResultVo.setPossibleKeys((String) ResultSet_getString.invoke(resultSet, "possible_keys"));
                explainResultVo.setKey((String) ResultSet_getString.invoke(resultSet, "key"));
                explainResultVo.setKeyLen((String) ResultSet_getString.invoke(resultSet, "key_len"));
                explainResultVo.setRef((String) ResultSet_getString.invoke(resultSet, "ref"));
                explainResultVo.setRows((String) ResultSet_getString.invoke(resultSet, "rows"));
                explainResultVo.setExtra((String) ResultSet_getString.invoke(resultSet, "Extra"));

                explainResultList.add(explainResultVo);
            }
            ResultSet_close.invoke(resultSet);
            Statement_close.invoke(stmt);

            // 打印结果
            analyzeResult(sql, explainResultList, printSQL);
        } catch (Exception se) {
            Log.error(se, "exception: %s\n[%s]", se.toString(), sql);
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
        if (StringUtils.containsAny(sql, Config.filterSqlKeywords)) {
            Log.debug("unsupported explain - filter out by [type] keywords: %s", Arrays.asList(Config.filterSqlKeywords));
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
    private static void analyzeResult(String sql, List<ExplainResultVo> explainResultList, boolean printSQL) {
        boolean needPrint = false;

        // * 打印所有结果
        if (Arrays.asList(Config.typeOptimizationItems).contains("*") || Arrays.asList(Config.extraOptimizationItems).contains("*")) {
            needPrint = true;
        } else {
            for (ExplainResultVo resultVo : explainResultList) {
                if (StringUtils.containsAny(resultVo.getType(), Config.typeOptimizationItems)) {
                    needPrint = true;
                    break;
                }
                if (StringUtils.containsAny(resultVo.getExtra(), Config.extraOptimizationItems)) {
                    needPrint = true;
                    break;
                }
            }
        }

        if (needPrint) {
            ExplainHelper.printExplainResult(sql, explainResultList, printSQL);
        }
    }
}