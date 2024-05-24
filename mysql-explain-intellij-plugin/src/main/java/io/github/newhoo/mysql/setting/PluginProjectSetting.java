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

    private final PropertiesComponent projectSetting;
    private final PropertiesComponent globalSetting;

    public PluginProjectSetting(Project project) {
        this.projectSetting = PropertiesComponent.getInstance(project);
        this.globalSetting = PropertiesComponent.getInstance();
    }


    public boolean getExistMysqlJar() {
        return projectSetting.getBoolean(KEY_EXIST_MYSQL_JAR, Boolean.FALSE);
    }

    public void setExistMysqlJar(boolean existMysqlJar) {
        projectSetting.setValue(KEY_EXIST_MYSQL_JAR, existMysqlJar);
    }

    public String getAgentPath() {
        return globalSetting.getValue(KEY_AGENT_PATH);
    }

    public void setAgentPath(String agentPath) {
        globalSetting.setValue(KEY_AGENT_PATH, agentPath);
    }

    public boolean getEnableMySQLExplain() {
        return projectSetting.getBoolean(KEY_MYSQL_EXPLAIN_ENABLE, Boolean.FALSE);
    }

    public void setEnableMySQLExplain(boolean enableMySQLExplain) {
        projectSetting.setValue(KEY_MYSQL_EXPLAIN_ENABLE, enableMySQLExplain);
    }

    public boolean getMysqlShowSql() {
        return projectSetting.getBoolean(KEY_MYSQL_SHOW_SQL, Boolean.FALSE);
    }

    public void setMysqlShowSql(boolean mysqlShowSql) {
        projectSetting.setValue(KEY_MYSQL_SHOW_SQL, mysqlShowSql, Boolean.FALSE);
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