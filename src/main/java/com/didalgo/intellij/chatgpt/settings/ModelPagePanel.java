package com.didalgo.intellij.chatgpt.settings;

import com.didalgo.intellij.chatgpt.ChatGptBundle;
import com.didalgo.intellij.chatgpt.OpenAIServiceHolder;
import com.didalgo.intellij.chatgpt.text.encryption.AES;
import com.intellij.openapi.options.Configurable;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.collections.ListUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public abstract class ModelPagePanel implements Configurable {
    protected JPanel myMainPanel;
    protected JPanel apiKeyTitledBorderBox;
    protected JBTextField apiKeyField;
    protected JComboBox<String> comboCombobox;
    protected JPanel modelTitledBorderBox;
    protected JCheckBox enableContextCheckBox;
    protected JLabel contextLabel;
    protected JCheckBox enableTokenConsumptionCheckBox;
    protected JCheckBox enableStreamResponseCheckBox;
    protected JLabel tokenLabel;
    protected JPanel urlTitledBox;
    protected JCheckBox enableCustomizeUrlCheckBox;
    protected TextFieldWithHistory customizeServerField;
    protected JPanel customizeServerOptions;
    protected JLabel apiEndpointLabel;

    public ModelPagePanel() {
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
        enableCustomizeUrlCheckBox.addItemListener(proxyTypeChangedListener);
        enableCustomizeServerOptions(false);
        initHelp();
    }

    private void enableCustomizeServerOptions(boolean enabled) {
        UIUtil.setEnabled(customizeServerOptions, enabled, true);
        apiEndpointLabel.setEnabled(false);
    }

    protected abstract OpenAISettingsState.OpenAIConfig getModelPageConfig(OpenAISettingsState state);

    @Override
    public void reset() {
        OpenAISettingsState state = OpenAISettingsState.getInstance();
        OpenAISettingsState.OpenAIConfig config = getModelPageConfig(state);
        apiKeyField.setText(AES.decrypt(config.getApiKey()));
        comboCombobox.setSelectedItem(config.getModelName());
        enableContextCheckBox.setSelected(config.isEnableContext());
        enableTokenConsumptionCheckBox.setSelected(config.isEnableTokenConsumption());
        enableStreamResponseCheckBox.setSelected(config.isEnableStreamResponse());
        enableCustomizeUrlCheckBox.setSelected(config.isEnableCustomApiEndpointUrl());
        customizeServerField.setHistory(config.getApiEndpointUrlHistory());
        customizeServerField.setText(config.getApiEndpointUrl());
    }

    @Override
    public @Nullable JComponent createComponent() {
        return myMainPanel;
    }

    @Override
    public boolean isModified() {
        OpenAISettingsState state = OpenAISettingsState.getInstance();
        OpenAISettingsState.OpenAIConfig config = getModelPageConfig(state);

        return !config.getApiKey().equals(AES.encrypt(apiKeyField.getText())) ||
                !config.getModelName().equals(comboCombobox.getSelectedItem()) ||
                config.isEnableContext() != enableContextCheckBox.isSelected() ||
                config.isEnableTokenConsumption() != enableTokenConsumptionCheckBox.isSelected() ||
                config.isEnableStreamResponse() != enableStreamResponseCheckBox.isSelected() ||
                config.isEnableCustomApiEndpointUrl() != enableCustomizeUrlCheckBox.isSelected() ||
                !config.getApiEndpointUrl().equals(customizeServerField.getText());
    }

    @Override
    public void apply() {
        OpenAISettingsState state = OpenAISettingsState.getInstance();
        OpenAISettingsState.OpenAIConfig config = getModelPageConfig(state);

        config.setApiKey(AES.encrypt(apiKeyField.getText()));
        config.setModelName(comboCombobox.getSelectedItem().toString());
        config.setEnableContext(enableContextCheckBox.isSelected());
        config.setEnableTokenConsumption(enableTokenConsumptionCheckBox.isSelected());
        config.setEnableStreamResponse(enableStreamResponseCheckBox.isSelected());
        config.setEnableCustomApiEndpointUrl(enableCustomizeUrlCheckBox.isSelected());
        config.setApiEndpointUrl(customizeServerField.getText());
        if (!config.getApiEndpointUrlHistory().contains(config.getApiEndpointUrl()))
            customizeServerField.addCurrentTextToHistory();
        config.setApiEndpointUrlHistory(ListUtils.union(customizeServerField.getHistory(), config.getApiEndpointUrlHistory()));
        OpenAIServiceHolder.refresh();
    }

    @Override
    public String getDisplayName() {
        return ChatGptBundle.message("ui.setting.menu.text");
    }

    private void createUIComponents() {
        apiKeyTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator tsUrl = new TitledSeparator("API Key Settings");
        apiKeyTitledBorderBox.add(tsUrl, BorderLayout.CENTER);

        modelTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator mdUrl = new TitledSeparator("Other Settings");
        modelTitledBorderBox.add(mdUrl, BorderLayout.CENTER);

        urlTitledBox = new JPanel(new BorderLayout());
        TitledSeparator url = new TitledSeparator("Server Settings");
        urlTitledBox.add(url, BorderLayout.CENTER);
    }

    private void initHelp() {
        JBFont smallFont = JBUI.Fonts.smallFont();
        Color smallFontForeground = UIUtil.getContextHelpForeground();
        contextLabel.setFont(smallFont);
        contextLabel.setForeground(smallFontForeground);

        tokenLabel.setFont(smallFont);
        tokenLabel.setForeground(smallFontForeground);

        apiEndpointLabel.setFont(smallFont);
        apiEndpointLabel.setForeground(smallFontForeground);
    }
}
