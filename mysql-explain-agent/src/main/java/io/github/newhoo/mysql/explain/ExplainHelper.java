package io.github.newhoo.mysql.explain;

import github.clyoudu.consoletable.ConsoleTable;
import github.clyoudu.consoletable.table.Cell;
import io.github.newhoo.mysql.common.Log;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ExplainHelper
 *
 * @author huzunrong
 * @since 1.0
 */
class ExplainHelper {

    static void printSQL(String sql, boolean showSQL) {
        if (showSQL) {
            System.out.println("EXECUTE SQL: \nEXPLAIN " + sql);
        }
    }

    static void printExplainResult(String sql, ExplainResultVo explainResult, boolean showSQL) {
        printSQL(sql, !showSQL);

        List<List<Cell>> body = explainResult.getValueColumnsList().stream()
                                             .map(explainResultVo -> explainResultVo.stream()
                                                                                    .map(Cell::new)
                                                                                    .collect(Collectors.toList())
                                             ).collect(Collectors.toList());

        try {
            new ConsoleTable.ConsoleTableBuilder()
                    .addHeaders(explainResult.getHeaderColumns().stream().map(Cell::new).collect(Collectors.toList()))
                    .addRows(body)
                    .build()
                    .print();
        } catch (Exception e) {
            Log.error(e, "print explain result exception: %s", e.toString());
        }
    }
}