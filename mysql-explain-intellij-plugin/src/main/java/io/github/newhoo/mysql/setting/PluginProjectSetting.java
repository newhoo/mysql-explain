package io.github.newhoo.mysql.setting;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;

/**
 * PluginProjectSetting
 *
 * @author huzunrong
 * @since 1.0
 */
public class PluginProjectSetting {

    private static final String KEY_MYSQL_EXPLAIN_ENABLE = "mysql-explain.enableMySQLExplain";
    private static final String KEY_MYSQL_SHOW_SQL = "mysql-explain.mysql.showSQL";
    private static final String KEY_MYSQL_PRINT_SQL_FILTER = "mysql-explain.mysql.showSQL.filter";
    private static final String KEY_MYSQL_FILTER = "mysql-explain.mysql.filter";
    private static final String KEY_MYSQL_TYPES = "mysql-explain.mysql.types";
    private static final String KEY_MYSQL_EXTRAS = "mysql-explain.mysql.extras";

    private final PropertiesComponent projectSetting;

    public PluginProjectSetting(Project project) {
        this.projectSetting = PropertiesComponent.getInstance(project);
    }

    public boolean getEnableMySQLExplain() {
        return projectSetting.getBoolean(KEY_MYSQL_EXPLAIN_ENABLE, Boolean.TRUE);
    }

    public void setEnableMySQLExplain(boolean enableMySQLExplain) {
        projectSetting.setValue(KEY_MYSQL_EXPLAIN_ENABLE, enableMySQLExplain, Boolean.TRUE);
    }

    public boolean getMysqlShowSql() {
        return projectSetting.getBoolean(KEY_MYSQL_SHOW_SQL, Boolean.TRUE);
    }

    public void setMysqlShowSql(boolean mysqlShowSql) {
        projectSetting.setValue(KEY_MYSQL_SHOW_SQL, mysqlShowSql, Boolean.TRUE);
    }

    public String getPrintSqlFilter() {
        return projectSetting.getValue(KEY_MYSQL_PRINT_SQL_FILTER, "");
    }

    public void setPrintSqlFilter(String printSqlFilter) {
        projectSetting.setValue(KEY_MYSQL_PRINT_SQL_FILTER, printSqlFilter);
    }

    public String getMysqlFilter() {
        return projectSetting.getValue(KEY_MYSQL_FILTER, "INSERT,UPDATE,DELETE");
    }

    public void setMysqlFilter(String mysqlFilter) {
        projectSetting.setValue(KEY_MYSQL_FILTER, mysqlFilter);
    }

    public String getMysqlTypes() {
        return projectSetting.getValue(KEY_MYSQL_TYPES, "ALL");
    }

    public void setMysqlTypes(String mysqlTypes) {
        projectSetting.setValue(KEY_MYSQL_TYPES, mysqlTypes);
    }

    public String getMysqlExtras() {
        return projectSetting.getValue(KEY_MYSQL_EXTRAS, "Using filesort,Using temporary");
    }

    public void setMysqlExtras(String mysqlExtras) {
        projectSetting.setValue(KEY_MYSQL_EXTRAS, mysqlExtras);
    }

}