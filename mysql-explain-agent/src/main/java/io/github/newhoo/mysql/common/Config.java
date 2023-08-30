package io.github.newhoo.mysql.common;

import github.clyoudu.consoletable.ConsoleTable.ConsoleTableBuilder;
import github.clyoudu.consoletable.enums.NullPolicy;
import github.clyoudu.consoletable.table.Cell;
import io.github.newhoo.mysql.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
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
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_SHOW_SQL_FILTER;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_TYPES;

/**
 * Config
 *
 * @author huzunrong
 * @since 1.0
 */
public final class Config {

    private static final Properties properties = new Properties();

    public static boolean showSQL;                  // 是否打印SQL
    public static String[] showSQLFilterKeywords;   // 打印前按关键词过滤
    public static String[] filterSqlKeywords;       // 执行前按关键词过滤
    public static String[] typeOptimizationItems;   // 执行结果按[type]关键词过滤
    public static String[] extraOptimizationItems;  // 执行结果按[Extra]关键词过滤

    static {
        String propertiesFile = System.getProperty(CUSTOM_PROPERTIES_FILENAME, DEFAULT_PROPERTIES_FILENAME);

        if (!StringUtils.isEmpty(propertiesFile)) {
            if (!propertiesFile.endsWith(".properties")) {
                Log.debug("config file must be properties file: %s", propertiesFile);
            } else {
                try {
                    // load from file
                    File file = new File(propertiesFile);
                    if (file.exists() && file.isFile()) {
                        Log.debug("try read config file: %s", propertiesFile);
                        InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                        properties.load(reader);
                    }
                    // load from file in classpath
                    else {
                        InputStream inputStream = ClassLoader.getSystemResourceAsStream(propertiesFile);
                        if (inputStream != null) {
                            Log.debug("try read config file in classpath: %s", propertiesFile);
                            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                            properties.load(reader);
                        }
                    }
                } catch (Exception e) {
                    Log.error(e, "parse config file exception: %s, %s", propertiesFile, e.toString());
                }
            }
        }
    }

    public static void init() {
        String showSqlStr = properties.getProperty(PROPERTIES_KEY_MYSQL_SHOW_SQL);
        if (showSqlStr == null) {
            showSqlStr = System.getProperty(PROPERTIES_KEY_MYSQL_SHOW_SQL, "false");
            Log.debug("load parameter [%s] from %s: %s", PROPERTIES_KEY_MYSQL_SHOW_SQL, "jvm parameter", System.getProperty(PROPERTIES_KEY_MYSQL_SHOW_SQL, ""));
        } else {
            Log.debug("load parameter [%s] from %s: %s", PROPERTIES_KEY_MYSQL_SHOW_SQL, "file", showSqlStr);
        }
        showSQL = !"0".equals(showSqlStr) && !"false".equals(showSqlStr);

        // 打印前按关键词过滤
        Set<String> showSQLFilterKeywordSet = new LinkedHashSet<>();
        putSplit(showSQLFilterKeywordSet, properties.getProperty(PROPERTIES_KEY_MYSQL_SHOW_SQL_FILTER, ""), PROPERTIES_KEY_MYSQL_SHOW_SQL_FILTER, "file");
        putSplit(showSQLFilterKeywordSet, System.getProperty(PROPERTIES_KEY_MYSQL_SHOW_SQL_FILTER), PROPERTIES_KEY_MYSQL_SHOW_SQL_FILTER, "jvm parameter");
        showSQLFilterKeywords = showSQLFilterKeywordSet.toArray(new String[0]);

        // 执行前按关键词过滤
        Set<String> filterSqlKeywordSet = new LinkedHashSet<>();
        putSplit(filterSqlKeywordSet, properties.getProperty(PROPERTIES_KEY_MYSQL_FILTER, ""), PROPERTIES_KEY_MYSQL_FILTER, "file");
        putSplit(filterSqlKeywordSet, System.getProperty(PROPERTIES_KEY_MYSQL_FILTER), PROPERTIES_KEY_MYSQL_FILTER, "jvm parameter");
        filterSqlKeywords = filterSqlKeywordSet.toArray(new String[0]);

        // 执行结果按[type]关键词过滤
        // 依次从最优到最差分别为：system > const > eq_ref > ref > fulltext > ref_or_null > index_merge > unique_subquery > index_subquery > range > index > ALL
        Set<String> typeOptimizationItemSet = Stream.of("ALL").collect(Collectors.toSet());
        putSplit(typeOptimizationItemSet, properties.getProperty(PROPERTIES_KEY_MYSQL_TYPES, ""), PROPERTIES_KEY_MYSQL_TYPES, "file");
        putSplit(typeOptimizationItemSet, System.getProperty(PROPERTIES_KEY_MYSQL_TYPES), PROPERTIES_KEY_MYSQL_TYPES, "jvm parameter");
        typeOptimizationItems = typeOptimizationItemSet.toArray(new String[0]);

        // 执行结果按[Extra]关键词过滤
        Set<String> extraOptimizationItemSet = Stream.of("Using filesort", "Using temporary").collect(Collectors.toSet());
        putSplit(extraOptimizationItemSet, properties.getProperty(PROPERTIES_KEY_MYSQL_EXTRAS, ""), PROPERTIES_KEY_MYSQL_EXTRAS, "file");
        putSplit(extraOptimizationItemSet, System.getProperty(PROPERTIES_KEY_MYSQL_EXTRAS), PROPERTIES_KEY_MYSQL_EXTRAS, "jvm parameter");
        extraOptimizationItems = extraOptimizationItemSet.toArray(new String[0]);
    }

    private static void putSplit(Collection<String> collection, String str, String name, String from) {
        if (collection == null || StringUtils.isEmpty(str)) {
            return;
        }
        Log.debug("load parameter [%s] from %s: %s", name, from, str);
        List<String> strings = Arrays.asList(str.split(","));
        if (!strings.isEmpty()) {
            collection.addAll(strings);
        }
    }

    public static String getMySQLConfTable() {
        List<Cell> header = new ArrayList<Cell>() {{
            add(new Cell("#"));
            add(new Cell("config item"));
            add(new Cell("config key"));
            add(new Cell("current value"));
            add(new Cell("default value"));
            add(new Cell("remark"));
        }};
        List<List<Cell>> body = new ArrayList<List<Cell>>() {{
            add(new ArrayList<Cell>() {{
                add(new Cell("1"));
                add(new Cell("Print SQL"));
                add(new Cell(PROPERTIES_KEY_MYSQL_SHOW_SQL));
                add(new Cell(showSQL + ""));
                add(new Cell("false"));
                add(new Cell("true/false"));
            }});
            add(new ArrayList<Cell>() {{
                add(new Cell("2"));
                add(new Cell("Filter out before print"));
                add(new Cell(PROPERTIES_KEY_MYSQL_SHOW_SQL_FILTER));
                add(new Cell(String.join(",", showSQLFilterKeywords)));
                add(new Cell(null));
                add(new Cell("Example: QRTZ_,COUNT(0)"));
            }});
            add(new ArrayList<Cell>() {{
                add(new Cell("3"));
                add(new Cell("Filter out before explain"));
                add(new Cell(PROPERTIES_KEY_MYSQL_FILTER));
                add(new Cell(String.join(",", filterSqlKeywords)));
                add(new Cell(null));
                add(new Cell("Example: INSERT,UPDATE,DELETE"));
            }});
            add(new ArrayList<Cell>() {{
                add(new Cell("4"));
                add(new Cell("Filter by explain [type]"));
                add(new Cell(PROPERTIES_KEY_MYSQL_TYPES));
                add(new Cell(String.join(",", typeOptimizationItems)));
                add(new Cell("ALL"));
                add(new Cell("system,const,eq_ref,ref,range,index,ALL"));
            }});
            add(new ArrayList<Cell>() {{
                add(new Cell("5"));
                add(new Cell("Filter by explain [Extra]"));
                add(new Cell(PROPERTIES_KEY_MYSQL_EXTRAS));
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