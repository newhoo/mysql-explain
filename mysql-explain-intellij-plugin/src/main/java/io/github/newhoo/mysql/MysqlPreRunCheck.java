package io.github.newhoo.mysql;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.JavaProgramPatcher;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import io.github.newhoo.mysql.setting.PluginProjectSetting;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_EXTRAS;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_FILTER;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_SHOW_SQL;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_TYPES;

/**
 * MysqlPreRunCheck
 *
 * @author huzunrong
 * @since 1.0
 */
public class MysqlPreRunCheck extends JavaProgramPatcher {

    private static final Logger logger = Logger.getInstance("mysql-explain");

    @Override
    public void patchJavaParameters(Executor executor, RunProfile configuration, JavaParameters javaParameters) {
        if (configuration instanceof RunConfiguration) {
            RunConfiguration runConfiguration = (RunConfiguration) configuration;
            PluginProjectSetting pluginProjectSetting = new PluginProjectSetting(runConfiguration.getProject());

            logger.info("检查mysql explain agent启用状态");
            if (pluginProjectSetting.getEnableMySQLExplain()
                    && findPsiClass("com.mysql.jdbc.PreparedStatement", runConfiguration.getProject()) != null) {
                ParametersList vmParametersList = javaParameters.getVMParametersList();

                String agentPath = getAgentPath();
                if (StringUtils.contains(agentPath, " ")) {
                    agentPath = "\"" + agentPath + "\"";
                }
                vmParametersList.addParametersString("-javaagent:" + agentPath);

                vmParametersList.addNotEmptyProperty(PROPERTIES_KEY_MYSQL_SHOW_SQL, String.valueOf(pluginProjectSetting.getMysqlShowSql()));
                vmParametersList.addNotEmptyProperty(PROPERTIES_KEY_MYSQL_FILTER, base64Encode(pluginProjectSetting.getMysqlFilter()));
                vmParametersList.addNotEmptyProperty(PROPERTIES_KEY_MYSQL_TYPES, pluginProjectSetting.getMysqlTypes());
                vmParametersList.addNotEmptyProperty(PROPERTIES_KEY_MYSQL_EXTRAS, pluginProjectSetting.getMysqlExtras());
            }
        }
    }

    /**
     * -javaagent:/path/your/quick-dev-jr-plugin.jar
     */
    private static String getAgentPath() {
        URL resource = io.github.newhoo.mysql.common.Constant.class.getResource("");
        if (resource != null && "jar".equals(resource.getProtocol())) {
            String path = resource.getPath();
            try {
                return URLDecoder.decode(path.substring("file:/".length() - 1, path.indexOf("!/")), "UTF-8");
            } catch (Exception e) {
                logger.error("URLDecoder Exception: " + resource.getPath(), e);
            }
        }
        return "";
    }

    /**
     * 查找类
     *
     * @param typeCanonicalText 参数类型全限定名称
     * @param project 当前project
     * @return 查找到的类
     */
    private static PsiClass findPsiClass(String typeCanonicalText, Project project) {
        String className = typeCanonicalText;
        if (className.contains("[]")) {
            className = className.replaceAll("\\[]", "");
        }
        if (className.contains("<")) {
            className = className.substring(0, className.indexOf("<"));
        }
        if (className.lastIndexOf(".") > 0) {
            className = className.substring(className.lastIndexOf(".") + 1);
        }
        PsiClass[] classesByName = PsiShortNamesCache.getInstance(project).getClassesByName(className, GlobalSearchScope.allScope(project));
        for (PsiClass psiClass : classesByName) {
            if (typeCanonicalText.startsWith(psiClass.getQualifiedName())) {
                return psiClass;
            }
        }
        return null;
    }

    private static String base64Encode(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }
}