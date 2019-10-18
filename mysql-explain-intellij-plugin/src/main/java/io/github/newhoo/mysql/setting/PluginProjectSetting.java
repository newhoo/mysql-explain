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
    private static final String KEY_MYSQL_FILTER = "mysql-explain.mysql.filter";
    private static final String KEY_MYSQL_TYPES = "mysql-explain.mysql.types";
    private static final String KEY_MYSQL_EXTRAS = "mysql-explain.mysql.extras";

    private final PropertiesComponent propertiesComponent;

    public PluginProjectSetting(Project project) {
        this.propertiesComponent = PropertiesComponent.getInstance(project);
    }

    // mysql explain

    public boolean getEnableMySQLExplain() {
        return propertiesComponent.getBoolean(KEY_MYSQL_EXPLAIN_ENABLE, Boolean.TRUE);
    }

    public void setEnableMySQLExplain(boolean enableMySQLExplain) {
        propertiesComponent.setValue(KEY_MYSQL_EXPLAIN_ENABLE, enableMySQLExplain, Boolean.TRUE);
    }

    public boolean getMysqlShowSql() {
        return propertiesComponent.getBoolean(KEY_MYSQL_SHOW_SQL, Boolean.FALSE);
    }

    public void setMysqlShowSql(boolean mysqlShowSql) {
        propertiesComponent.setValue(KEY_MYSQL_SHOW_SQL, mysqlShowSql, Boolean.FALSE);
    }

    public String getMysqlFilter() {
        return propertiesComponent.getValue(KEY_MYSQL_FILTER, "QRTZ_,COUNT(0)");
    }

    public void setMysqlFilter(String mysqlFilter) {
        propertiesComponent.setValue(KEY_MYSQL_FILTER, mysqlFilter);
    }

    public String getMysqlTypes() {
        return propertiesComponent.getValue(KEY_MYSQL_TYPES, "ALL");
    }

    public void setMysqlTypes(String mysqlTypes) {
        propertiesComponent.setValue(KEY_MYSQL_TYPES, mysqlTypes);
    }

    public String getMysqlExtras() {
        return propertiesComponent.getValue(KEY_MYSQL_EXTRAS, "Using filesort,Using temporary");
    }

    public void setMysqlExtras(String mysqlExtras) {
        propertiesComponent.setValue(KEY_MYSQL_EXTRAS, mysqlExtras);
    }

}