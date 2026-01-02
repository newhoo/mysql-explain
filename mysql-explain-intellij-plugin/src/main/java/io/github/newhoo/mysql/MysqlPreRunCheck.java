package io.github.newhoo.mysql;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.JavaProgramPatcher;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.github.newhoo.mysql.setting.PluginProjectSetting;

import java.util.Set;
import java.util.stream.Stream;

import static io.github.newhoo.mysql.common.Constant.*;
import static java.util.stream.Collectors.toSet;

/**
 * MysqlPreRunCheck
 *
 * @author huzunrong
 * @since 1.0
 */
public class MysqlPreRunCheck extends JavaProgramPatcher {

    private static final Logger logger = Logger.getInstance("mysql-explain");
    private static final Set<String> NOT_SUPPORTED_RUN_CONFIGURATION = Stream.of(
            "org.jetbrains.idea.maven.execution.MavenRunConfiguration"
    ).collect(toSet());

    @Override
    public void patchJavaParameters(Executor executor, RunProfile configuration, JavaParameters javaParameters) {
        if (configuration instanceof RunConfiguration) {
            if (NOT_SUPPORTED_RUN_CONFIGURATION.contains(configuration.getClass().getName())) {
                return;
            }
            RunConfiguration runConfiguration = (RunConfiguration) configuration;
            PluginProjectSetting setting = new PluginProjectSetting(runConfiguration.getProject());

            if (!checkCondition(setting, runConfiguration.getProject())) {
                return;
            }

            ParametersList vmParametersList = javaParameters.getVMParametersList();

            String agentPath = JavaToolHelper.getMySQLExplainAgentPath();
            if (!agentPath.isEmpty()) {
                vmParametersList.addParametersString("\"-javaagent:" + agentPath + "\"");

                vmParametersList.addNotEmptyProperty(PROPERTIES_KEY_MYSQL_SHOW_SQL, String.valueOf(setting.getMysqlShowSql()));
                vmParametersList.addNotEmptyProperty(PROPERTIES_KEY_MYSQL_SHOW_SQL_FILTER, setting.getPrintSqlFilter());
                vmParametersList.addNotEmptyProperty(PROPERTIES_KEY_MYSQL_FILTER, setting.getMysqlFilter());
                vmParametersList.addNotEmptyProperty(PROPERTIES_KEY_MYSQL_TYPES, setting.getMysqlTypes());
                vmParametersList.addNotEmptyProperty(PROPERTIES_KEY_MYSQL_EXTRAS, setting.getMysqlExtras());

                logger.info("run parameters: " + vmParametersList);
            }
        }
    }

    private boolean checkCondition(PluginProjectSetting setting, Project project) {
        logger.info("Check mysql explain plugin status.");
        if (!setting.getEnableMySQLExplain()) {
            return false;
        }
        // 存在mysql驱动
        if (!JavaToolHelper.existMysqlJar(project)) {
            return false;
        }
        return true;
    }
}