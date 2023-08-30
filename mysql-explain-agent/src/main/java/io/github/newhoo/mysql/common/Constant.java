package io.github.newhoo.mysql.common;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Constant
 *
 * @author huzunrong
 * @since 1.0
 */
public class Constant {

    public static final Set<String> SUPPORTED_EXPLAIN_SQL = Stream.of(
            "SELECT", "UPDATE", "INSERT", "DELETE", "REPLACE", "select", "update", "insert", "delete", "replace"
    ).collect(Collectors.toSet());

    public static final String DEFAULT_PROPERTIES_FILENAME = "mysql-explain.properties";

    public static final String CUSTOM_PROPERTIES_FILENAME = "mysql-explain-properties-file";

    public static final String PROPERTIES_KEY_MYSQL_SHOW_SQL = "mysql.showSQL";
    public static final String PROPERTIES_KEY_MYSQL_SHOW_SQL_FILTER = "mysql.showSQL.filter";
    public static final String PROPERTIES_KEY_MYSQL_FILTER = "mysql.explain.filter";
    public static final String PROPERTIES_KEY_MYSQL_TYPES = "mysql.explain.types";
    public static final String PROPERTIES_KEY_MYSQL_EXTRAS = "mysql.explain.extras";
}