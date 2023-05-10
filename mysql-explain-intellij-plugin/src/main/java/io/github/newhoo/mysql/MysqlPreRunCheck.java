package io.github.newhoo.mysql;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.JavaProgramPatcher;
import com.intellij.openapi.diagnostic.Logger;
import io.github.newhoo.mysql.setting.PluginProjectSetting;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Stream;

import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_EXTRAS;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_FILTER;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_SHOW_SQL;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_TYPES;
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

            logger.info("检查mysql explain agent启用状态");
            if (setting.getEnableMySQLExplain() && setting.getExistMysqlJar()) {
                ParametersList vmParametersList = javaParameters.getVMParametersList();

                String agentPath = setting.getAgentPath();
                if (StringUtils.isNotEmpty(agentPath)) {
                    if (StringUtils.contains(agentPath, " ")) {
                        agentPath = "\"" + agentPath + "\"";
                    }
                    vmParametersList.addParametersString("-javaagent:" + agentPath);

                    vmParametersList.addNotEmptyProperty(PROPERTIES_KEY_MYSQL_SHOW_SQL, String.valueOf(setting.getMysqlShowSql()));
                    vmParametersList.addNotEmptyProperty(PROPERTIES_KEY_MYSQL_FILTER, base64Encode(setting.getMysqlFilter()));
                    vmParametersList.addNotEmptyProperty(PROPERTIES_KEY_MYSQL_TYPES, setting.getMysqlTypes());
                    vmParametersList.addNotEmptyProperty(PROPERTIES_KEY_MYSQL_EXTRAS, setting.getMysqlExtras());
                }
            }
        }
    }

    private static String base64Encode(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }
}