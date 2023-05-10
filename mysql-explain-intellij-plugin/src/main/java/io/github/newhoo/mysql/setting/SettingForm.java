package io.github.newhoo.mysql.setting;

import io.github.newhoo.mysql.i18n.ExplainBundle;

import javax.swing.*;

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

    private JLabel preFilterLabel;
    private JLabel preFilterTipLabel;
    public JTextField mysqlFilterText;

    private JLabel filterByTypeLabel;
    private JLabel filterByTypeTipLabel;
    public JTextField mysqlTypesText;

    private JLabel filterByExtraLabel;
    private JLabel filterByExtraTipLabel;
    public JTextField mysqlExtrasText;

    public SettingForm() {
        mysqlExplainEnableCheckbox.setText(ExplainBundle.getMessage("plugin.setting.enable"));
        mysqlShowSqlCheckBox.setText(ExplainBundle.getMessage("plugin.setting.printSQL"));
        mysqlShowSqlCheckBox.setToolTipText(ExplainBundle.getMessage("plugin.setting.printSQLTip"));

        preFilterLabel.setText(ExplainBundle.getMessage("plugin.setting.preFilterLabel"));
        preFilterTipLabel.setText(ExplainBundle.getMessage("plugin.setting.preFilterTipLabel"));

        filterByTypeLabel.setText(ExplainBundle.getMessage("plugin.setting.filterByTypeLabel"));
        filterByTypeTipLabel.setText(ExplainBundle.getMessage("plugin.setting.filterByTypeTipLabel"));

        filterByExtraLabel.setText(ExplainBundle.getMessage("plugin.setting.filterByExtraLabel"));
        filterByExtraTipLabel.setText(ExplainBundle.getMessage("plugin.setting.filterByExtraTipLabel"));
    }
}