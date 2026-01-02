package io.github.newhoo.mysql.setting;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import io.github.newhoo.mysql.JavaToolHelper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * SettingConfigurable
 *
 * @author huzunrong
 * @since 1.0
 */
public class SettingConfigurable implements Configurable {

    private final PluginProjectSetting projectSetting;
    private final SettingForm settingForm;
    private final Project project;

    public SettingConfigurable(Project project) {
        this.project = project;
        this.projectSetting = new PluginProjectSetting(project);
        this.settingForm = new SettingForm(project, projectSetting);
    }

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "MySQL Explain";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return settingForm.mainPanel;
    }

    @Override
    public boolean isModified() {
        return !String.valueOf(projectSetting.getEnableMySQLExplain()).equals(String.valueOf(settingForm.mysqlExplainEnableCheckbox.isSelected()))
                || !String.valueOf(projectSetting.getMysqlShowSql()).equals(String.valueOf(settingForm.mysqlShowSqlCheckBox.isSelected()))
                || !JavaToolHelper.equals(projectSetting.getPrintSqlFilter(), settingForm.printSqlFilterTextField.getText())
                || !JavaToolHelper.equals(projectSetting.getMysqlFilter(), settingForm.mysqlFilterText.getText())
                || !JavaToolHelper.equals(projectSetting.getMysqlTypes(), settingForm.mysqlTypesText.getText())
                || !JavaToolHelper.equals(projectSetting.getMysqlExtras(), settingForm.mysqlExtrasText.getText());
    }

    @Override
    public void apply() {
        projectSetting.setEnableMySQLExplain(settingForm.mysqlExplainEnableCheckbox.isSelected());
        projectSetting.setMysqlShowSql(settingForm.mysqlShowSqlCheckBox.isSelected());
        projectSetting.setPrintSqlFilter(settingForm.printSqlFilterTextField.getText());
        projectSetting.setMysqlFilter(settingForm.mysqlFilterText.getText());
        projectSetting.setMysqlTypes(settingForm.mysqlTypesText.getText());
        projectSetting.setMysqlExtras(settingForm.mysqlExtrasText.getText());
    }

    @Override
    public void reset() {
        settingForm.reset(project);
    }
}