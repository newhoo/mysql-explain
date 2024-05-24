package io.github.newhoo.mysql;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.github.newhoo.mysql.setting.PluginProjectSetting;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;


/**
 * 启动后判断
 */
public class MyStartupActivity implements ProjectActivity {

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "MySQL explain check", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                PluginProjectSetting setting = new PluginProjectSetting(project);

                DumbService.getInstance(project)
                           .runReadActionInSmartMode(() -> {
                               if (!setting.getExistMysqlJar()) {
                                   boolean existMysqlJar = existMysqlJar(project);
                                   System.out.println("[mysql-explain] check exists mysql connector driver: " + existMysqlJar);
                                   setting.setExistMysqlJar(existMysqlJar);
                                   if (existMysqlJar) {
                                       setting.setEnableMySQLExplain(true);
                                   }
                               }

                               if (StringUtils.isNotEmpty(setting.getAgentPath())) {
                                   if (!StringUtils.contains(setting.getAgentPath(), "mysql-explain-agent-1.1.1-with-dependencies.jar")) {
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
                           });
            }
        });
        return null;
    }

    /**
     * -javaagent:/path/your/mysql-explain-agent.jar
     */
    public static Optional<String> getAgentPath(String pluginId, String agentName) {
        PluginId pluginId0 = PluginId.getId(pluginId);
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(pluginId0);
        if (plugin != null) {
            Path pluginPath = plugin.getPluginPath();
            if (pluginPath == null) {
                return Optional.empty();
            }
            return Arrays.stream(Objects.requireNonNull(pluginPath.toFile().listFiles()))
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
