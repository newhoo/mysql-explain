package io.github.newhoo.mysql.setting;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import io.github.newhoo.mysql.i18n.ExplainBundle;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_EXTRAS;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_FILTER;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_SHOW_SQL;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_SHOW_SQL_FILTER;
import static io.github.newhoo.mysql.common.Constant.PROPERTIES_KEY_MYSQL_TYPES;

/**
 * SettingForm
 *
 * @author huzunrong
 * @since 1.0
 */
public class SettingForm {
    public JPanel mainPanel;

    public JCheckBox mysqlExplainEnableCheckbox;
    public JCheckBox mysqlShowSqlCheckBox;
    public JTextField printSqlFilterTextField;
    private JLabel printSqlFilterTipLabel;

    private JLabel preFilterLabel;
    private JLabel preFilterTipLabel;
    public JTextField mysqlFilterText;

    private JLabel filterByTypeLabel;
    private JLabel filterByTypeTipLabel;
    public JTextField mysqlTypesText;

    private JLabel filterByExtraLabel;
    private JLabel filterByExtraTipLabel;
    public JTextField mysqlExtrasText;

    private JPanel previewPanel;
    private JLabel previewLabel;
    public JTextArea previewTextArea;

    private final PluginProjectSetting projectSetting;

    public SettingForm(Project project, PluginProjectSetting projectSetting) {
        this.projectSetting = projectSetting;

        mysqlExplainEnableCheckbox.setText(ExplainBundle.getMessage("plugin.setting.enable"));
        if (!projectSetting.getExistMysqlJar() && project.isDefault()) {
            mysqlExplainEnableCheckbox.setToolTipText(ExplainBundle.getMessage("plugin.setting.enableTip"));
        }
        mysqlShowSqlCheckBox.setText(ExplainBundle.getMessage("plugin.setting.printSQL"));
        printSqlFilterTipLabel.setText(ExplainBundle.getMessage("plugin.setting.printSqlFilterTipLabel"));

        preFilterLabel.setText(ExplainBundle.getMessage("plugin.setting.preFilterLabel"));
        preFilterTipLabel.setText(ExplainBundle.getMessage("plugin.setting.preFilterTipLabel"));

        filterByTypeLabel.setText(ExplainBundle.getMessage("plugin.setting.filterByTypeLabel"));
        filterByTypeTipLabel.setText(ExplainBundle.getMessage("plugin.setting.filterByTypeTipLabel"));

        filterByExtraLabel.setText(ExplainBundle.getMessage("plugin.setting.filterByExtraLabel"));
        filterByExtraTipLabel.setText(ExplainBundle.getMessage("plugin.setting.filterByExtraTipLabel"));

        previewLabel.setText(ExplainBundle.getMessage("plugin.setting.previewLabel"));

        addFocusListener(printSqlFilterTextField);
        addFocusListener(mysqlFilterText);
        addFocusListener(mysqlTypesText);
        addFocusListener(mysqlExtrasText);

        mysqlExplainEnableCheckbox.addItemListener(e -> setPreview());
        mysqlShowSqlCheckBox.addItemListener(e -> setPreview());
        if (StringUtils.isEmpty(projectSetting.getAgentPath())) {
            return;
        }
        previewLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 生成快捷按钮
                DefaultActionGroup generateActionGroup = new DefaultActionGroup(
                        new AnAction("Copy as Line String") {
                            @Override
                            public @NotNull ActionUpdateThread getActionUpdateThread() {
                                return ActionUpdateThread.BGT;
                            }

                            @Override
                            public void actionPerformed(@NotNull AnActionEvent e) {
                                String s = previewTextArea.getText().replace("\\", "\\\\").replace("\n", " ");
                                CopyPasteManager.getInstance().setContents(new StringSelection(s));
                            }
                        },
                        new AnAction("Copy as String Array") {
                            @Override
                            public @NotNull ActionUpdateThread getActionUpdateThread() {
                                return ActionUpdateThread.BGT;
                            }

                            @Override
                            public void actionPerformed(@NotNull AnActionEvent e) {
                                String s = previewTextArea.getText().replace("\\", "\\\\").replace("\n", ",\n    ");
                                CopyPasteManager.getInstance().setContents(new StringSelection("[\n    " + s + "\n]"));
                            }
                        }
                );

                DataContext dataContext = DataManager.getInstance().getDataContext(previewLabel);
                final ListPopup popup = JBPopupFactory.getInstance()
                                                      .createActionGroupPopup(
                                                              null,
                                                              generateActionGroup,
                                                              dataContext,
                                                              JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                                                              true);
                popup.showInBestPositionFor(dataContext);
            }
        });
    }

    private void addFocusListener(JTextField jTextField) {
        jTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                setPreview();
            }

            @Override
            public void focusGained(FocusEvent e) {
            }
        });
    }

    public void reset(Project project) {
        mysqlExplainEnableCheckbox.setSelected(projectSetting.getEnableMySQLExplain());
        mysqlShowSqlCheckBox.setSelected(projectSetting.getMysqlShowSql());
        printSqlFilterTextField.setText(projectSetting.getPrintSqlFilter());
        mysqlFilterText.setText(projectSetting.getMysqlFilter());
        mysqlTypesText.setText(projectSetting.getMysqlTypes());
        mysqlExtrasText.setText(projectSetting.getMysqlExtras());

        setPreview();
    }

    private void setPreview() {
        boolean selected = mysqlExplainEnableCheckbox.isSelected();
        previewPanel.setVisible(selected);

        if (!selected) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        String agentPath = projectSetting.getAgentPath();
        if (StringUtils.isEmpty(agentPath)) {
            sb.append("Agent not found! Try check your project with mysql connector driver and reopen it.");
        } else {
            sb.append("\"").append("-javaagent:").append(agentPath).append("\"\n");

            sb.append("\"").append("-D").append(PROPERTIES_KEY_MYSQL_SHOW_SQL).append("=").append(mysqlShowSqlCheckBox.isSelected()).append("\"\n");
            sb.append("\"").append("-D").append(PROPERTIES_KEY_MYSQL_SHOW_SQL_FILTER).append("=").append(printSqlFilterTextField.getText()).append("\"\n");
            sb.append("\"").append("-D").append(PROPERTIES_KEY_MYSQL_FILTER).append("=").append(mysqlFilterText.getText()).append("\"\n");
            sb.append("\"").append("-D").append(PROPERTIES_KEY_MYSQL_TYPES).append("=").append(mysqlTypesText.getText()).append("\"\n");
            sb.append("\"").append("-D").append(PROPERTIES_KEY_MYSQL_EXTRAS).append("=").append(mysqlExtrasText.getText()).append("\"");
        }
        previewTextArea.setText(sb.toString());
    }
}