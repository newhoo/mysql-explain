package io.github.newhoo.mysql.explain;

import io.github.newhoo.mysql.common.Constant;
import io.github.newhoo.mysql.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.newhoo.mysql.common.Config.extraOptimizationItems;
import static io.github.newhoo.mysql.common.Config.filterSqlKeywords;
import static io.github.newhoo.mysql.common.Config.showSQL;
import static io.github.newhoo.mysql.common.Config.typeOptimizationItems;

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
     * @param sql
     */
    public static void explainSql(Object conn, String sql) {
        if (conn == null || sql == null) {
            System.out.println(231312);
            return;
        }
        if (!processBefore(sql)) {
            return;
        }
        try {
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
                explainResultVo.setPartitions((String) ResultSet_getString.invoke(resultSet, "partitions"));
                explainResultVo.setType((String) ResultSet_getString.invoke(resultSet, "type"));
                explainResultVo.setPossibleKeys((String) ResultSet_getString.invoke(resultSet, "possible_keys"));
                explainResultVo.setKey((String) ResultSet_getString.invoke(resultSet, "key"));
                explainResultVo.setKeyLen((String) ResultSet_getString.invoke(resultSet, "key_len"));
                explainResultVo.setRef((String) ResultSet_getString.invoke(resultSet, "ref"));
                explainResultVo.setRows((String) ResultSet_getString.invoke(resultSet, "rows"));
                explainResultVo.setFiltered((String) ResultSet_getString.invoke(resultSet, "filtered"));
                explainResultVo.setExtra((String) ResultSet_getString.invoke(resultSet, "Extra"));

                explainResultList.add(explainResultVo);
            }
            ResultSet_close.invoke(resultSet);
            Statement_close.invoke(stmt);

            // 打印结果
            analyzeResult(sql, explainResultList);
        } catch (Exception se) {
            System.err.println("EXPLAIN SQL异常: " + se.toString());
            se.printStackTrace();
        }
    }

    /**
     * 前置检查，检查是否执行计划
     */
    private static boolean processBefore(String sql) {
        String[] s = sql.substring(0, 10).split(" ", 2);
        if (StringUtils.isEmptyArray(s)) {
            if (isDebug()) {
                System.out.println("不支持的EXPLAIN: sql.split(\" \", 2) empty");
            }
            return false;
        }
        if (StringUtils.containsAny(sql, filterSqlKeywords)) {
            if (isDebug()) {
                System.out.println("不支持的EXPLAIN - 关键词跳过: " + Arrays.asList(filterSqlKeywords));
            }
            return false;
        }
        if (!Constant.SUPPORTED_EXPLAIN_SQL.contains(s[0])) {
            if (!s[0].startsWith("SELECT*") && !s[0].startsWith("select*")) {
                if (isDebug()) {
                    System.out.println(("不支持的EXPLAIN - 不支持的语句: " + s[0] + ", " + Constant.SUPPORTED_EXPLAIN_SQL));
                }
                return false;
            }
        }

        // 打印SQL
        ExplainHelper.printSQL(sql, showSQL);
        return true;
    }

    /**
     * 分析结果
     */
    private static void analyzeResult(String sql, List<ExplainResultVo> explainResultList) {
        boolean needPrint = false;

        for (ExplainResultVo resultVo : explainResultList) {
            if (StringUtils.containsAny(resultVo.getType(), typeOptimizationItems)) {
                needPrint = true;
                break;
            }
            if (StringUtils.containsAny(resultVo.getExtra(), extraOptimizationItems)) {
                needPrint = true;
                break;
            }
        }
        // 打印所有结果
        if (!needPrint) {
            if (Arrays.asList(typeOptimizationItems).contains("*") || Arrays.asList(extraOptimizationItems).contains("*")) {
                needPrint = true;
            }
        }

        if (needPrint) {
            ExplainHelper.printExplainResult(sql, explainResultList, showSQL);
        }
    }

    private static boolean isDebug() {
        return System.getProperty("debug") != null;
    }
}