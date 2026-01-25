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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static io.github.newhoo.mysql.common.Constant.CUSTOM_PROPERTIES_FILENAME;
import static io.github.newhoo.mysql.common.Constant.DEFAULT_PROPERTIES_FILENAME;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_EXTRAS;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_FILTER;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_SHOW_SQL;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_SHOW_SQL_FILTER;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_TYPES;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_USE_TRADITIONAL_FORMAT;

/**
 * Config
 *
 * @author huzunrong
 * @since 1.0
 */
public final class Config {

    public static boolean isDebug;                  // 是否debug
    public static boolean useTraditionalFormat;     // 显示地添加 FORMAT=TRADITIONAL
    public static boolean showSQL;                  // 是否打印SQL
    public static String[] showSQLFilterKeywords;   // 打印前按关键词过滤
    public static String[] filterSqlKeywords;       // 执行前按关键词过滤
    public static String[] typeOptimizationItems;   // 执行结果按[type]关键词过滤
    public static String[] extraOptimizationItems;  // 执行结果按[Extra]关键词过滤

    public static void init() {
        isDebug = System.getProperty("debug") != null;

        final Properties FILE_PROPERTIES = new Properties();
        // 尝试解析配置文件
        {
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
                            FILE_PROPERTIES.load(reader);
                        }
                        // load from file in classpath
                        else {
                            InputStream inputStream = ClassLoader.getSystemResourceAsStream(propertiesFile);
                            if (inputStream != null) {
                                Log.debug("try read config file in classpath: %s", propertiesFile);
                                InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                                FILE_PROPERTIES.load(reader);
                            }
                        }
                    } catch (Exception e) {
                        Log.error(e, "parse config file exception: %s, %s", propertiesFile, e.toString());
                    }
                }
            }
        }

        // 显示地添加 FORMAT=TRADITIONAL
        {
            String useTraditionalFormatStr = FILE_PROPERTIES.getProperty(PROPERTIES_KEY_MYSQL_USE_TRADITIONAL_FORMAT);
            if (useTraditionalFormatStr != null) {
                Log.debug("load parameter [%s] from %s: %s", PROPERTIES_KEY_MYSQL_USE_TRADITIONAL_FORMAT, "file", useTraditionalFormatStr);
            } else {
                useTraditionalFormatStr = System.getProperty(PROPERTIES_KEY_MYSQL_USE_TRADITIONAL_FORMAT);
                if (useTraditionalFormatStr != null) {
                    Log.debug("load parameter [%s] from %s: %s", PROPERTIES_KEY_MYSQL_USE_TRADITIONAL_FORMAT, "jvm parameter", useTraditionalFormatStr);
                } else {
                    useTraditionalFormatStr = System.getenv(PROPERTIES_KEY_MYSQL_USE_TRADITIONAL_FORMAT);
                    if (useTraditionalFormatStr != null) {
                        Log.debug("load parameter [%s] from %s: %s", PROPERTIES_KEY_MYSQL_USE_TRADITIONAL_FORMAT, "environment", useTraditionalFormatStr);
                    }
                }
            }
            useTraditionalFormat = useTraditionalFormatStr != null && !useTraditionalFormatStr.isEmpty() && !"0".equals(useTraditionalFormatStr) && !"false".equals(useTraditionalFormatStr);
        }

        // 打印SQL
        {
            String showSqlStr = FILE_PROPERTIES.getProperty(PROPERTIES_KEY_MYSQL_SHOW_SQL);
            if (showSqlStr != null) {
                Log.debug("load parameter [%s] from %s: %s", PROPERTIES_KEY_MYSQL_SHOW_SQL, "file", showSqlStr);
            } else {
                showSqlStr = System.getProperty(PROPERTIES_KEY_MYSQL_SHOW_SQL);
                if (showSqlStr != null) {
                    Log.debug("load parameter [%s] from %s: %s", PROPERTIES_KEY_MYSQL_SHOW_SQL, "jvm parameter", showSqlStr);
                } else {
                    showSqlStr = System.getenv(PROPERTIES_KEY_MYSQL_SHOW_SQL);
                    if (showSqlStr != null) {
                        Log.debug("load parameter [%s] from %s: %s", PROPERTIES_KEY_MYSQL_SHOW_SQL, "environment", showSqlStr);
                    }
                }
            }
            showSQL = showSqlStr != null && !showSqlStr.isEmpty() && !"0".equals(showSqlStr) && !"false".equals(showSqlStr);
        }

        // 打印前按关键词过滤
        {
            Set<String> showSQLFilterKeywordSet = new LinkedHashSet<>();
            putSplit(showSQLFilterKeywordSet, FILE_PROPERTIES.getProperty(PROPERTIES_KEY_MYSQL_SHOW_SQL_FILTER, ""), PROPERTIES_KEY_MYSQL_SHOW_SQL_FILTER, "file");
            putSplit(showSQLFilterKeywordSet, System.getProperty(PROPERTIES_KEY_MYSQL_SHOW_SQL_FILTER), PROPERTIES_KEY_MYSQL_SHOW_SQL_FILTER, "jvm parameter");
            showSQLFilterKeywords = showSQLFilterKeywordSet.toArray(new String[0]);
        }

        // 执行前按关键词过滤
        {
            Set<String> filterSqlKeywordSet = new LinkedHashSet<>();
            putSplit(filterSqlKeywordSet, FILE_PROPERTIES.getProperty(PROPERTIES_KEY_MYSQL_FILTER, ""), PROPERTIES_KEY_MYSQL_FILTER, "file");
            putSplit(filterSqlKeywordSet, System.getProperty(PROPERTIES_KEY_MYSQL_FILTER), PROPERTIES_KEY_MYSQL_FILTER, "jvm parameter");
            putSplit(filterSqlKeywordSet, System.getenv(PROPERTIES_KEY_MYSQL_FILTER), PROPERTIES_KEY_MYSQL_FILTER, "environment");
            filterSqlKeywords = filterSqlKeywordSet.toArray(new String[0]);
        }

        // 执行结果按[type]关键词过滤
        // 依次从最优到最差分别为：system > const > eq_ref > ref > fulltext > ref_or_null > index_merge > unique_subquery > index_subquery > range > index > ALL
        {
            Set<String> typeOptimizationItemSet = new HashSet<>();
            putSplit(typeOptimizationItemSet, FILE_PROPERTIES.getProperty(PROPERTIES_KEY_MYSQL_TYPES, ""), PROPERTIES_KEY_MYSQL_TYPES, "file");
            putSplit(typeOptimizationItemSet, System.getProperty(PROPERTIES_KEY_MYSQL_TYPES), PROPERTIES_KEY_MYSQL_TYPES, "jvm parameter");
            putSplit(typeOptimizationItemSet, System.getenv(PROPERTIES_KEY_MYSQL_TYPES), PROPERTIES_KEY_MYSQL_TYPES, "environment");
            if (typeOptimizationItemSet.isEmpty()) {
                typeOptimizationItemSet.add("ALL");
            }
            typeOptimizationItems = typeOptimizationItemSet.toArray(new String[0]);
        }

        // 执行结果按[Extra]关键词过滤
        {
            Set<String> extraOptimizationItemSet = new HashSet<>();
            putSplit(extraOptimizationItemSet, FILE_PROPERTIES.getProperty(PROPERTIES_KEY_MYSQL_EXTRAS, ""), PROPERTIES_KEY_MYSQL_EXTRAS, "file");
            putSplit(extraOptimizationItemSet, System.getProperty(PROPERTIES_KEY_MYSQL_EXTRAS), PROPERTIES_KEY_MYSQL_EXTRAS, "jvm parameter");
            putSplit(extraOptimizationItemSet, System.getenv(PROPERTIES_KEY_MYSQL_EXTRAS), PROPERTIES_KEY_MYSQL_EXTRAS, "environment");
            if (extraOptimizationItemSet.isEmpty()) {
                extraOptimizationItemSet.add("Using filesort");
                extraOptimizationItemSet.add("Using temporary");
            }
            extraOptimizationItems = extraOptimizationItemSet.toArray(new String[0]);
        }
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