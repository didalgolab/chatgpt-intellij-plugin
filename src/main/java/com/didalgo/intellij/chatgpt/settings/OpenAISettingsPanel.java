/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.settings;

import com.didalgo.intellij.chatgpt.ChatGptToolWindowFactory;
import com.didalgo.intellij.chatgpt.ModelCategory;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.didalgo.intellij.chatgpt.ChatGptBundle;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OpenAISettingsPanel implements Configurable {
    private JPanel myMainPanel;
    private JPanel connectionTitledBorderBox;
    private JBTextField readTimeoutField;
    private JPanel proxyTitledBorderBox;
    private JCheckBox enableAvatarCheckBox;
    private JPanel contentTitledBorderBox;
    private JComboBox<String> firstCombobox;
    private JComboBox<String> secondCombobox;
    private JCheckBox enableLineWarpCheckBox;
    private JLabel readTimeoutHelpLabel;
    private JLabel contentOrderHelpLabel;
    private JPanel openaiAssistantTitledBorderBox;
    private final String[] comboboxItemsString = {
            ChatGptToolWindowFactory.GPT35_TURBO_CONTENT_NAME,
            ChatGptToolWindowFactory.ONLINE_CHATGPT_CONTENT_NAME};
    private boolean needRestart = false;

    public static final String CREATE_API_KEY = "https://api.openai.com/dashboard/user/api_keys";

    public OpenAISettingsPanel() {
        init();
    }

    private void init() {
        readTimeoutField.getEmptyText().setText(ChatGptBundle.message("ui.setting.connection.read_timeout.empty_text"));

        firstCombobox.setModel(new DefaultComboBoxModel<>(comboboxItemsString));
        secondCombobox.setModel(new DefaultComboBoxModel<>(comboboxItemsString));
    }

    @Override
    public void reset() {
        OpenAISettingsState state = OpenAISettingsState.getInstance();
        readTimeoutField.setText(state.getReadTimeout());
        enableAvatarCheckBox.setSelected(state.isEnableAvatar());
        firstCombobox.setSelectedItem(state.contentOrder.get(1));
        secondCombobox.setSelectedItem(state.contentOrder.get(2));
        enableLineWarpCheckBox.setSelected(state.isEnableLineWarp());
        initHelp();
    }

    @Override
    public @Nullable JComponent createComponent() {
        return myMainPanel;
    }

    @Override
    public boolean isModified() {
        OpenAISettingsState state = OpenAISettingsState.getInstance();

        // If you change the order, you need to restart the IDE to take effect
        needRestart = !StringUtil.equals(state.contentOrder.get(1), (String)firstCombobox.getSelectedItem())||
                !StringUtil.equals(state.contentOrder.get(2), (String)secondCombobox.getSelectedItem())||
                !state.isEnableLineWarp() == enableLineWarpCheckBox.isSelected();

        return
                !StringUtil.equals(state.getReadTimeout(), readTimeoutField.getText()) ||
                !state.isEnableAvatar() == enableAvatarCheckBox.isSelected() ||
                !StringUtil.equals(state.contentOrder.get(1), (String)firstCombobox.getSelectedItem()) ||
                !StringUtil.equals(state.contentOrder.get(2), (String)secondCombobox.getSelectedItem()) ||
                !state.isEnableLineWarp() == enableLineWarpCheckBox.isSelected();
    }

    @Override
    public void apply() {
        OpenAISettingsState state = OpenAISettingsState.getInstance();

        boolean readTimeoutIsNumber = StringUtils.isNumeric(readTimeoutField.getText());
        state.setReadTimeout(!readTimeoutIsNumber ? "50000" : readTimeoutField.getText());
        state.setEnableAvatar(enableAvatarCheckBox.isSelected());

        String firstSelected = (String) firstCombobox.getSelectedItem();
        String secondSelected = (String) secondCombobox.getSelectedItem();

        // Determine whether each location has a different Content
        List<String> strings = new ArrayList<>(3);
        strings.add(firstSelected);
        strings.add(secondSelected);
        List<String> collect = strings.stream().distinct().toList();
        if (collect.size() != strings.size()) {
            MessageDialogBuilder.yesNo("Duplicate Content exists!", "The content of " +
                            "each position must be unique, please re-adjust the order")
                    .yesText("Ok")
                    .noText("Close").ask(myMainPanel);
            return;
        }

        state.contentOrder.put(1, firstSelected);
        state.contentOrder.put(2, secondSelected);
        state.setEnableLineWarp(enableLineWarpCheckBox.isSelected());

        if (needRestart) {
            boolean yes = MessageDialogBuilder.yesNo("Content order changed!", "Changing " +
                            "the content order requires restarting the IDE to take effect. Do you " +
                            "want to restart to apply the settings?")
                    .yesText("Restart")
                    .noText("Not Now").ask(myMainPanel);
            if (yes) {
                ApplicationManagerEx.getApplicationEx().restart(true);
            }
        }
    }

    @Override
    public String getDisplayName() {
        return ChatGptBundle.message("ui.setting.menu.text");
    }

    private void createUIComponents() {
        connectionTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator tsConnection = new TitledSeparator(ChatGptBundle.message("ui.setting.connection.title"));
        connectionTitledBorderBox.add(tsConnection,BorderLayout.CENTER);

        proxyTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator tsProxy = new TitledSeparator("Proxy Settings");
        proxyTitledBorderBox.add(tsProxy,BorderLayout.CENTER);

        contentTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator tsUrl = new TitledSeparator("Tool Window Settings");
        contentTitledBorderBox.add(tsUrl,BorderLayout.CENTER);

        openaiAssistantTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator oaUrl = new TitledSeparator("OpenAI Assistant");
        openaiAssistantTitledBorderBox.add(oaUrl,BorderLayout.CENTER);
    }

    public void initHelp() {
        readTimeoutHelpLabel.setFont(JBUI.Fonts.smallFont());
        readTimeoutHelpLabel.setForeground(UIUtil.getContextHelpForeground());

        contentOrderHelpLabel.setFont(JBUI.Fonts.smallFont());
        contentOrderHelpLabel.setForeground(UIUtil.getContextHelpForeground());
    }

    /**
     * Returns the appropriate settings panel {@code Class} for the given model category.
     *
     * @param category the model category to fetch the settings panel for
     * @return the appropriate {@code Class} for the settings panel
     */
    public static Class<? extends Configurable> getTargetPanelClassForCategory(String category) {
        return switch (category) {
            case ModelCategory.GPT_3_5 -> GPT3_35_TurboPanel.class;
            case ModelCategory.GPT_4 -> GPT4_Panel.class;
            default -> OpenAISettingsPanel.class;
        };
    }
}
