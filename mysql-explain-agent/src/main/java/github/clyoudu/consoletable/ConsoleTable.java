package github.clyoudu.consoletable;

import github.clyoudu.consoletable.enums.NullPolicy;
import github.clyoudu.consoletable.table.Body;
import github.clyoudu.consoletable.table.Cell;
import github.clyoudu.consoletable.table.Header;
import github.clyoudu.consoletable.util.StringPadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Create by IntelliJ IDEA
 * console table
 *
 * @author chenlei
 * @dateTime 2018/12/11 9:28
 * @description ConsoleTable
 */
public class ConsoleTable {

    private Header header;
    private Body body;
    String lineSep = "\n";
    String verticalSep = "|";
    String horizontalSep = "-";
    String joinSep = "+";
    int[] columnWidths;
    NullPolicy nullPolicy = NullPolicy.NULL_STRING;
    boolean restrict = false;

    private ConsoleTable() {}

    public void print() {
        System.out.println(getContent());
    }

    public String getContent() {
        return toString();
    }

    List<String> getLines() {
        List<String> lines = new ArrayList<>();
        if ((header != null && !header.isEmpty()) || (body != null && !body.isEmpty())) {
            lines.addAll(header.print(columnWidths, horizontalSep, verticalSep, joinSep));
            lines.addAll(body.print(columnWidths, horizontalSep, verticalSep, joinSep));
        }
        return lines;
    }

    @Override
    public String toString() {
        return getLines().stream().collect(Collectors.joining(lineSep));
    }

    public static class ConsoleTableBuilder {

        ConsoleTable consoleTable = new ConsoleTable();

        public ConsoleTableBuilder() {
            consoleTable.header = new Header();
            consoleTable.body = new Body();
        }

        public ConsoleTableBuilder addHead(Cell cell) {
            consoleTable.header.addHead(cell);
            return this;
        }

        public ConsoleTableBuilder addRow(List<Cell> row) {
            consoleTable.body.addRow(row);
            return this;
        }

        public ConsoleTableBuilder addHeaders(List<Cell> headers) {
            consoleTable.header.addHeads(headers);
            return this;
        }

        public ConsoleTableBuilder addRows(List<List<Cell>> rows) {
            consoleTable.body.addRows(rows);
            return this;
        }

        public ConsoleTableBuilder lineSep(String lineSep) {
            consoleTable.lineSep = lineSep;
            return this;
        }

        public ConsoleTableBuilder verticalSep(String verticalSep) {
            consoleTable.verticalSep = verticalSep;
            return this;
        }

        public ConsoleTableBuilder horizontalSep(String horizontalSep) {
            consoleTable.horizontalSep = horizontalSep;
            return this;
        }

        public ConsoleTableBuilder joinSep(String joinSep) {
            consoleTable.joinSep = joinSep;
            return this;
        }

        public ConsoleTableBuilder nullPolicy(NullPolicy nullPolicy) {
            consoleTable.nullPolicy = nullPolicy;
            return this;
        }

        public ConsoleTableBuilder restrict(boolean restrict) {
            consoleTable.restrict = restrict;
            return this;
        }

        public ConsoleTable build() {
            //compute max column widths
            if (!consoleTable.header.isEmpty() || !consoleTable.body.isEmpty()) {
                List<List<Cell>> allRows = new ArrayList<>();
                allRows.add(consoleTable.header.cells);
                allRows.addAll(consoleTable.body.rows);
                int maxColumn = allRows.stream().map(List::size).mapToInt(size -> size).max().getAsInt();
                int minColumn = allRows.stream().map(List::size).mapToInt(size -> size).min().getAsInt();
                if (maxColumn != minColumn && consoleTable.restrict) {
                    throw new IllegalArgumentException("number of columns for each row must be the same when strict mode used.");
                }
                consoleTable.columnWidths = new int[maxColumn];
                for (List<Cell> row : allRows) {
                    for (int i = 0; i < row.size(); i++) {
                        Cell cell = row.get(i);
                        if (cell == null || cell.getValue() == null) {
                            cell = consoleTable.nullPolicy.getCell(cell);
                            row.set(i, cell);
                        }
                        int length = StringPadUtil.strLength(cell.getValue());
                        if (consoleTable.columnWidths[i] < length) {
                            consoleTable.columnWidths[i] = length;
                        }
                    }
                }
            }
            return consoleTable;
        }
    }
}
