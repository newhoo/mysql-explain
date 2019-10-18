package io.github.newhoo.mysql.common;

import github.clyoudu.consoletable.ConsoleTable.ConsoleTableBuilder;
import github.clyoudu.consoletable.enums.NullPolicy;
import github.clyoudu.consoletable.table.Cell;
import io.github.newhoo.mysql.util.StringUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.newhoo.mysql.common.Constant.CUSTOM_PROPERTIES_FILENAME;
import static io.github.newhoo.mysql.common.Constant.DEFAULT_PROPERTIES_FILENAME;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_EXTRAS;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_FILTER;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_SHOW_SQL;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_TYPES;
import static io.github.newhoo.mysql.util.StringUtils.putSplit;

/**
 * Config
 *
 * @author huzunrong
 * @since 1.0
 */
public final class Config {

    private static Properties properties = new Properties();

    public static boolean showSQL;
    public static String[] filterSqlKeywords;
    public static String[] typeOptimizationItems;
    public static String[] extraOptimizationItems;

    static {
        String propertiesFile = System.getProperty(CUSTOM_PROPERTIES_FILENAME, DEFAULT_PROPERTIES_FILENAME);

        if (!StringUtils.isEmpty(propertiesFile)) {
            try {
                InputStream inputStream = ClassLoader.getSystemResourceAsStream(propertiesFile);
                if (inputStream != null) {
                    InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    properties.load(reader);
                }
            } catch (Exception e) {
                System.err.println("解析配置文件信息异常: " + e.toString());
            }
        }
    }

    public static void init() {
        String showSqlStr = properties.getProperty(PROPERTIES_KEY_MYSQL_SHOW_SQL);
        if (showSqlStr == null) {
            showSqlStr = System.getProperty(PROPERTIES_KEY_MYSQL_SHOW_SQL, "false");
        }
        showSQL = !"0".equals(showSqlStr) && !"false".equals(showSqlStr);

        // filter
        Set<String> filterSqlKeywordSet = new HashSet<>();
        putSplit(filterSqlKeywordSet, properties.getProperty(PROPERTIES_KEY_MYSQL_FILTER, ""));
        putSplit(filterSqlKeywordSet, System.getProperty(PROPERTIES_KEY_MYSQL_FILTER));
        filterSqlKeywords = filterSqlKeywordSet.toArray(new String[0]);

        // 优化项：type
        // 依次从最优到最差分别为：system > const > eq_ref > ref > fulltext > ref_or_null > index_merge > unique_subquery > index_subquery > range > index > ALL
        Set<String> typeOptimizationItemSet = Stream.of("ALL").collect(Collectors.toSet());
        putSplit(typeOptimizationItemSet, properties.getProperty(PROPERTIES_KEY_MYSQL_TYPES, ""));
        putSplit(typeOptimizationItemSet, System.getProperty(PROPERTIES_KEY_MYSQL_TYPES));
        typeOptimizationItems = typeOptimizationItemSet.toArray(new String[0]);

        // 优化项：Extra
        Set<String> extraOptimizationItemSet = Stream.of("Using filesort", "Using temporary").collect(Collectors.toSet());
        putSplit(extraOptimizationItemSet, properties.getProperty(PROPERTIES_KEY_MYSQL_EXTRAS, ""));
        putSplit(extraOptimizationItemSet, System.getProperty(PROPERTIES_KEY_MYSQL_EXTRAS));
        extraOptimizationItems = extraOptimizationItemSet.toArray(new String[0]);
    }

    public static String getMySQLConfTable() {
        List<Cell> header = new ArrayList<Cell>() {{
            add(new Cell("#"));
            add(new Cell("config item"));
            add(new Cell("current value"));
            add(new Cell("default value"));
            add(new Cell("remark"));
        }};
        List<List<Cell>> body = new ArrayList<List<Cell>>() {{
            add(new ArrayList<Cell>() {{
                add(new Cell("1"));
                add(new Cell("Print SQL"));
                add(new Cell(showSQL + ""));
                add(new Cell("false"));
                add(new Cell("true/false"));
            }});
            add(new ArrayList<Cell>() {{
                add(new Cell("2"));
                add(new Cell("Filter SQL by keywords"));
                add(new Cell(String.join(",", filterSqlKeywords)));
                add(new Cell(null));
                add(new Cell("Example: QRTZ_,COUNT(0)"));
            }});
            add(new ArrayList<Cell>() {{
                add(new Cell("3"));
                add(new Cell("Optimization item by type"));
                add(new Cell(String.join(",", typeOptimizationItems)));
                add(new Cell("ALL"));
                add(new Cell("system > const > eq_ref > ref > range > index > ALL"));
            }});
            add(new ArrayList<Cell>() {{
                add(new Cell("4"));
                add(new Cell("Optimization item by Extra"));
                add(new Cell(String.join(",", extraOptimizationItems)));
                add(new Cell("Using filesort,Using temporary"));
                add(new Cell("Using filesort,Using temporary,Using where,Using index condition"));
            }});
        }};

        return new ConsoleTableBuilder()
                .nullPolicy(NullPolicy.EMPTY_STRING)
                .addHeaders(header)
                .addRows(body)
                .build().getContent();
    }
}