package io.github.newhoo.mysql.explain;

import github.clyoudu.consoletable.ConsoleTable;
import github.clyoudu.consoletable.table.Cell;
import io.github.newhoo.mysql.common.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ExplainHelper
 *
 * @author huzunrong
 * @since 1.0
 */
class ExplainHelper {

    private static final List<Cell> EXPLAIN_HEADER = new ArrayList<Cell>() {{
        add(new Cell("id"));
        add(new Cell("select_type"));
        add(new Cell("table"));
        add(new Cell("partitions"));
        add(new Cell("type"));
        add(new Cell("possible_keys"));
        add(new Cell("key"));
        add(new Cell("key_len"));
        add(new Cell("ref"));
        add(new Cell("rows"));
        add(new Cell("filtered"));
        add(new Cell("Extra"));
    }};

    static void printSQL(String sql, boolean showSQL) {
        if (showSQL) {
            System.out.println("EXECUTE SQL: \nEXPLAIN " + sql);
        }
    }

    static void printExplainResult(String sql, List<ExplainResultVo> explainResultList, boolean showSQL) {
        printSQL(sql, !showSQL);

        List<List<Cell>> body = explainResultList.stream()
                                                 .map(explainResultVo -> {
                                                     return new ArrayList<Cell>() {{
                                                         add(new Cell(explainResultVo.getId()));
                                                         add(new Cell(explainResultVo.getSelectType()));
                                                         add(new Cell(explainResultVo.getTable()));
                                                         add(new Cell(explainResultVo.getPartitions()));
                                                         add(new Cell(explainResultVo.getType()));
                                                         add(new Cell(explainResultVo.getPossibleKeys()));
                                                         add(new Cell(explainResultVo.getKey()));
                                                         add(new Cell(explainResultVo.getKeyLen()));
                                                         add(new Cell(explainResultVo.getRef()));
                                                         add(new Cell(explainResultVo.getRows()));
                                                         add(new Cell(explainResultVo.getFiltered()));
                                                         add(new Cell(explainResultVo.getExtra()));
                                                     }};
                                                 }).collect(Collectors.toList());

        try {
            new ConsoleTable.ConsoleTableBuilder()
                    .addHeaders(EXPLAIN_HEADER)
                    .addRows(body)
                    .build()
                    .print();
        } catch (Exception e) {
            Log.error(e, "print explain result exception: %s", e.toString());
        }
    }
}