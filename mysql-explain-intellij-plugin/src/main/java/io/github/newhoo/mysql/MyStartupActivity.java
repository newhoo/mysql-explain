package io.github.newhoo.mysql;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.github.newhoo.mysql.setting.PluginProjectSetting;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;


/**
 * 启动后判断
 */
public class MyStartupActivity implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "MySQL Explain Check", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                PluginProjectSetting setting = new PluginProjectSetting(project);

                ApplicationManager.getApplication().runReadAction(() -> {
                    if (StringUtils.isNotEmpty(setting.getAgentPath())) {
                        if (!StringUtils.contains(setting.getAgentPath(), "mysql-explain-agent-1.1.0-jar-with-dependencies.jar")) {
                            setting.setAgentPath(null);
                        } else if (!new File(setting.getAgentPath()).exists()) {
                            setting.setAgentPath(null);
                        }
                    }
                    if (StringUtils.isEmpty(setting.getAgentPath())) {
                        AppExecutorUtil.getAppExecutorService().execute(() -> {
                            getAgentPath("io.github.newhoo.mysql-explain", "mysql-explain-agent")
                                    .ifPresent(s -> {
                                        System.out.println("[mysql-explain] set agent path: " + s);
                                        setting.setAgentPath(s);
                                    });
                        });
                    }

                    boolean existMysqlJar = existMysqlJar(project);
                    System.out.println("[mysql-explain] check exists mysql connector driver: " + existMysqlJar);
                    setting.setExistMysqlJar(existMysqlJar);
                });
            }
        });
    }

    /**
     * -javaagent:/path/your/bean-invoker-agent.jar
     */
    private static Optional<String> getAgentPath(String pluginId, String agentName) {
        PluginId pluginId0 = PluginId.getId(pluginId);
        IdeaPluginDescriptor plugin = PluginManager.getPlugin(pluginId0);
        if (plugin != null) {
//            Path pluginPath = plugin.getPluginPath();
            File path = plugin.getPath();
            return Arrays.stream(Objects.requireNonNull(path.listFiles()))
                         .filter(File::isDirectory)
                         .flatMap(file -> Arrays.stream(Objects.requireNonNull(file.listFiles(f -> f.getName().endsWith(".jar")))))
                         .filter(file -> file.getName().contains(agentName))
                         .map(File::getAbsolutePath)
                         .findFirst();
        }
        return Optional.empty();
    }

    private static boolean existMysqlJar(Project project) {
        return findPsiClass("com.mysql.jdbc.Driver", project) != null
                || findPsiClass("com.mysql.cj.jdbc.Driver", project) != null;
    }

    /**
     * 查找类
     *
     * @param typeCanonicalText 参数类型全限定名称
     * @param project           当前project
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
}
