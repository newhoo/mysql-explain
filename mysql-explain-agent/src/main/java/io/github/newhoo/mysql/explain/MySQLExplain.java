package io.github.newhoo.mysql.explain;

import io.github.newhoo.mysql.common.Constant;
import io.github.newhoo.mysql.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.Statement;
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
    private static final Logger logger = LoggerFactory.getLogger(MySQLExplain.class);

    /**
     * 调用：MySQLExplainCBP#process
     */
    public static void explainSql(com.mysql.jdbc.MySQLConnection conn, com.mysql.jdbc.Buffer sendPacket) {
        byte[] bytes = new byte[sendPacket.getPosition()];
        System.arraycopy(sendPacket.getByteBuffer(), 0, bytes, 0, bytes.length);

        String sql = new String(bytes, 5, bytes.length - 5);
        if (!processBefore(sql)) {
            return;
        }

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("EXPLAIN " + sql);

            List<ExplainResultVo> explainResultList = new ArrayList<>();
            while (rs.next()) {
                ExplainResultVo explainResultVo = new ExplainResultVo();
                explainResultVo.setId(rs.getString("id"));
                explainResultVo.setSelectType(rs.getString("select_type"));
                explainResultVo.setTable(rs.getString("table"));
                explainResultVo.setPartitions(rs.getString("partitions"));
                explainResultVo.setType(rs.getString("type"));
                explainResultVo.setPossibleKeys(rs.getString("possible_keys"));
                explainResultVo.setKey(rs.getString("key"));
                explainResultVo.setKeyLen(rs.getString("key_len"));
                explainResultVo.setRef(rs.getString("ref"));
                explainResultVo.setRows(rs.getString("rows"));
                explainResultVo.setFiltered(rs.getString("filtered"));
                explainResultVo.setExtra(rs.getString("Extra"));

                explainResultList.add(explainResultVo);
            }

            rs.close();

            // 打印结果
            analyzeResult(sql, explainResultList);
        } catch (Exception se) {
            logger.error("EXPLAIN SQL异常:{}", se.toString(), se);
        }
    }

    /**
     * 前置检查，检查是否执行计划
     */
    private static boolean processBefore(String sql) {
        String[] s = sql.substring(0, 10).split(" ", 2);
        if (StringUtils.isEmptyArray(s)) {
            logger.warn("不支持的EXPLAIN: sql.split(\" \", 2) empty");
            return false;
        }
        if (StringUtils.containsAny(sql, filterSqlKeywords)) {
            logger.debug("不支持的EXPLAIN - 关键词跳过: {}", Arrays.asList(filterSqlKeywords));
            return false;
        }
        if (!Constant.SUPPORTED_EXPLAIN_SQL.contains(s[0])) {
            if (!s[0].startsWith("SELECT*") && !s[0].startsWith("select*")) {
                logger.warn("不支持的EXPLAIN - 不支持的语句: {}, {}", s[0], Constant.SUPPORTED_EXPLAIN_SQL);
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
}