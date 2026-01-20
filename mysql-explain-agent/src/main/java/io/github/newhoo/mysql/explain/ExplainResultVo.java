package io.github.newhoo.mysql.explain;

import java.util.List;

/**
 * ExplainResultVo
 *
 * @author huzunrong
 * @since 1.0
 */
public class ExplainResultVo {

    private final List<String> headerColumns;
    private final List<List<String>> valueColumnsList;

    public ExplainResultVo(List<String> headerColumns, List<List<String>> valueColumnsList) {
        this.headerColumns = headerColumns;
        this.valueColumnsList = valueColumnsList;
    }

    public List<String> getHeaderColumns() {
        return headerColumns;
    }

    public List<List<String>> getValueColumnsList() {
        return valueColumnsList;
    }

    @Override
    public String toString() {
        return "ExplainResultVo{" +
                "headerColumns=" + headerColumns +
                ", valueColumnsList=" + valueColumnsList +
                '}';
    }
}