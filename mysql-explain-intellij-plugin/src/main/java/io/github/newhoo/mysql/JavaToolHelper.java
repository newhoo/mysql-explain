package io.github.newhoo.mysql;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class JavaToolHelper {

    public static boolean existMysqlJar(Project project) {
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

    @NotNull
    public static String getMySQLExplainAgentPath() {
        return getAgentPath("io.github.newhoo.mysql-explain", "mysql-explain-agent").orElse("");
    }

    /**
     * -javaagent:/path/your/mysql-explain-agent.jar
     */
    private static Optional<String> getAgentPath(String pluginId, String agentName) {
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId(pluginId));
        if (plugin != null) {
            Path pluginPath = plugin.getPluginPath();
            if (pluginPath == null) {
                return Optional.empty();
            }
            return Arrays.stream(Objects.requireNonNull(pluginPath.toFile().listFiles())).filter(File::isDirectory).flatMap(file -> Arrays.stream(Objects.requireNonNull(file.listFiles(f -> f.getName().endsWith(".jar"))))).filter(file -> file.getName().contains(agentName)).map(File::getAbsolutePath).findFirst();
        }
        return Optional.empty();
    }

    public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1.length() != cs2.length()) {
            return false;
        }
        if (cs1 instanceof String && cs2 instanceof String) {
            return cs1.equals(cs2);
        }
        // Step-wise comparison
        final int length = cs1.length();
        for (int i = 0; i < length; i++) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                return false;
            }
        }
        return true;
    }
}
