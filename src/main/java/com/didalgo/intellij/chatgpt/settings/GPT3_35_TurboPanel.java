/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.settings;

import com.didalgo.intellij.chatgpt.ChatGptBundle;
import com.didalgo.intellij.chatgpt.OpenAIServiceHolder;
import com.didalgo.intellij.chatgpt.text.encryption.AES;
import com.intellij.openapi.options.Configurable;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class GPT3_35_TurboPanel implements Configurable {
    private JPanel myMainPanel;
    private JPanel apiKeyTitledBorderBox;
    private JBTextField apiKeyField;
    private JComboBox<String> comboCombobox;
    private JPanel modelTitledBorderBox;
    private JCheckBox enableContextCheckBox;
    private JLabel contextLabel;
    private JCheckBox enableTokenConsumptionCheckBox;
    private JCheckBox enableStreamResponseCheckBox;
    private JLabel tokenLabel;
    private JPanel urlTitledBox;
    private JCheckBox enableCustomizeGpt35TurboUrlCheckBox;
    private JTextField customizeServerField;
    private JPanel customizeServerOptions;


    public GPT3_35_TurboPanel() {
        init();
    }

    private void init() {
        apiKeyField.getEmptyText().setText("Your API Key, find it in: https://platform.openai.com/account/api-keys");
        ItemListener proxyTypeChangedListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                enableCustomizeServerOptions(true);
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                enableCustomizeServerOptions(false);
            }
        };
        enableCustomizeGpt35TurboUrlCheckBox.addItemListener(proxyTypeChangedListener);
        enableCustomizeServerOptions(false);
        initHelp();
    }

    private void enableCustomizeServerOptions(boolean enabled) {
        UIUtil.setEnabled(customizeServerOptions, enabled, true);
    }

    @Override
    public void reset() {
        OpenAISettingsState state = OpenAISettingsState.getInstance();
        OpenAISettingsState.OpenAIConfig config = state.getGpt35Config();
        apiKeyField.setText(AES.decrypt(config.getApiKey()));
        comboCombobox.setSelectedItem(config.getModelName());
        enableContextCheckBox.setSelected(config.isEnableContext());
        enableTokenConsumptionCheckBox.setSelected(config.isEnableTokenConsumption());
        enableStreamResponseCheckBox.setSelected(config.isEnableGPT35StreamResponse());
        enableCustomizeGpt35TurboUrlCheckBox.setSelected(config.isEnableCustomizeGpt35TurboUrl());
        customizeServerField.setText(config.getGpt35TurboUrl());
    }

    @Override
    public @Nullable JComponent createComponent() {
        return myMainPanel;
    }

    @Override
    public boolean isModified() {
        OpenAISettingsState state = OpenAISettingsState.getInstance();
        OpenAISettingsState.OpenAIConfig config = state.getGpt35Config();

        return !config.getApiKey().equals(AES.encrypt(apiKeyField.getText())) ||
                !config.getModelName().equals(comboCombobox.getSelectedItem().toString()) ||
                config.isEnableContext() != enableContextCheckBox.isSelected() ||
                config.isEnableTokenConsumption() != enableTokenConsumptionCheckBox.isSelected() ||
                config.isEnableGPT35StreamResponse() != enableStreamResponseCheckBox.isSelected() ||
                config.isEnableCustomizeGpt35TurboUrl() != enableCustomizeGpt35TurboUrlCheckBox.isSelected() ||
                !config.getGpt35TurboUrl().equals(customizeServerField.getText());
    }

    @Override
    public void apply() {
        OpenAISettingsState state = OpenAISettingsState.getInstance();
        OpenAISettingsState.OpenAIConfig config = state.getGpt35Config();

        config.setApiKey(AES.encrypt(apiKeyField.getText()));
        config.setModelName(comboCombobox.getSelectedItem().toString());
        config.setEnableContext(enableContextCheckBox.isSelected());
        config.setEnableTokenConsumption(enableTokenConsumptionCheckBox.isSelected());
        config.setEnableGPT35StreamResponse(enableStreamResponseCheckBox.isSelected());
        config.setEnableCustomizeGpt35TurboUrl(enableCustomizeGpt35TurboUrlCheckBox.isSelected());
        config.setGpt35TurboUrl(customizeServerField.getText());
        OpenAIServiceHolder.refresh();
    }

    @Override
    public String getDisplayName() {
        return ChatGptBundle.message("ui.setting.menu.text");
    }

    private void createUIComponents() {
        apiKeyTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator tsUrl = new TitledSeparator("API Key Settings");
        apiKeyTitledBorderBox.add(tsUrl,BorderLayout.CENTER);

        modelTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator mdUrl = new TitledSeparator("Other Settings");
        modelTitledBorderBox.add(mdUrl,BorderLayout.CENTER);

        urlTitledBox = new JPanel(new BorderLayout());
        TitledSeparator url = new TitledSeparator("Server Settings");
        urlTitledBox.add(url,BorderLayout.CENTER);
    }

    private void initHelp() {
        contextLabel.setFont(JBUI.Fonts.smallFont());
        contextLabel.setForeground(UIUtil.getContextHelpForeground());

        tokenLabel.setFont(JBUI.Fonts.smallFont());
        tokenLabel.setForeground(UIUtil.getContextHelpForeground());
    }
}
