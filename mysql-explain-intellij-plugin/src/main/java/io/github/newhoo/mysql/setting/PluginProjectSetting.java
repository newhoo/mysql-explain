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

    private static final String KEY_AGENT_PATH = "mysql-explain.agentPath";
    private static final String KEY_EXIST_MYSQL_JAR = "mysql-explain.existMysqlJar";
    private static final String KEY_MYSQL_EXPLAIN_ENABLE = "mysql-explain.enableMySQLExplain";
    private static final String KEY_MYSQL_SHOW_SQL = "mysql-explain.mysql.showSQL";
    private static final String KEY_MYSQL_PRINT_SQL_FILTER = "mysql-explain.mysql.showSQL.filter";
    private static final String KEY_MYSQL_FILTER = "mysql-explain.mysql.filter";
    private static final String KEY_MYSQL_TYPES = "mysql-explain.mysql.types";
    private static final String KEY_MYSQL_EXTRAS = "mysql-explain.mysql.extras";

    private final PropertiesComponent propertiesComponent;

    public PluginProjectSetting(Project project) {
        this.propertiesComponent = PropertiesComponent.getInstance(project);
    }


    public boolean getExistMysqlJar() {
        return propertiesComponent.getBoolean(KEY_EXIST_MYSQL_JAR, Boolean.FALSE);
    }

    public void setExistMysqlJar(boolean existMysqlJar) {
        propertiesComponent.setValue(KEY_EXIST_MYSQL_JAR, existMysqlJar);
    }

    public String getAgentPath() {
        return propertiesComponent.getValue(KEY_AGENT_PATH);
    }

    public void setAgentPath(String agentPath) {
        propertiesComponent.setValue(KEY_AGENT_PATH, agentPath);
    }

    public boolean getEnableMySQLExplain() {
        return propertiesComponent.getBoolean(KEY_MYSQL_EXPLAIN_ENABLE, Boolean.FALSE);
    }

    public void setEnableMySQLExplain(boolean enableMySQLExplain) {
        propertiesComponent.setValue(KEY_MYSQL_EXPLAIN_ENABLE, enableMySQLExplain);
    }

    public boolean getMysqlShowSql() {
        return propertiesComponent.getBoolean(KEY_MYSQL_SHOW_SQL, Boolean.FALSE);
    }

    public void setMysqlShowSql(boolean mysqlShowSql) {
        propertiesComponent.setValue(KEY_MYSQL_SHOW_SQL, mysqlShowSql, Boolean.FALSE);
    }

    public String getPrintSqlFilter() {
        return propertiesComponent.getValue(KEY_MYSQL_PRINT_SQL_FILTER, "");
    }

    public void setPrintSqlFilter(String printSqlFilter) {
        propertiesComponent.setValue(KEY_MYSQL_PRINT_SQL_FILTER, printSqlFilter);
    }

    public String getMysqlFilter() {
        return propertiesComponent.getValue(KEY_MYSQL_FILTER, "INSERT,UPDATE,DELETE");
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