/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.settings;

import com.didalgo.intellij.chatgpt.ChatGptBundle;
import com.didalgo.intellij.chatgpt.Errors;
import com.didalgo.intellij.chatgpt.ParallelOptions;
import com.didalgo.intellij.chatgpt.StartupHandler;
import com.didalgo.intellij.chatgpt.chat.AssistantType;
import com.didalgo.intellij.chatgpt.chat.client.ChatClientFactory;
import com.didalgo.intellij.chatgpt.chat.client.ChatClientHolder;
import com.didalgo.intellij.chatgpt.chat.models.CustomModel;
import com.didalgo.intellij.chatgpt.chat.models.ModelType;
import com.didalgo.intellij.chatgpt.chat.models.StandardModel;
import com.didalgo.intellij.chatgpt.chat.models.api.ModelCatalogApi;
import com.didalgo.intellij.chatgpt.settings.GeneralSettings.AssistantOptions;
import com.didalgo.intellij.chatgpt.settings.auth.InMemoryCredentialStore;
import com.didalgo.intellij.chatgpt.ui.GUIKit;
import com.didalgo.intellij.chatgpt.ui.tool.window.ChatToolWindow;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.OptionAction;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBOptionButton;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public abstract class ModelPagePanel implements Configurable, Configurable.Composite {
    protected JPanel myMainPanel;
    protected JPanel apiKeyTitledBorderBox;
    protected JBPasswordField apiKeyField;
    protected JComboBox<String> comboCombobox;
    protected JPanel modelTitledBorderBox;
    protected JCheckBox enableStreamResponseCheckBox;
    protected JCheckBox enableStreamOptionsCheckBox;
    protected JPanel urlTitledBox;
    protected JCheckBox enableCustomizeUrlCheckBox;
    protected TextFieldWithHistory customizeServerField;
    protected JPanel customizeServerOptions;
    private JSpinner temperatureSpinner;
    private JSpinner topPSpinner;
    private JCheckBox enabledInAToolCheckBox;
    private JTextField azureApiEndpointField;
    private JTextField azureDeploymentNameField;
    private JLabel azureApiEndpointLabel;
    private JLabel azureDeploymentNameLabel;
    private JPanel customizeServerLabel;
    private JButton testConnectionButton;
    private JButton refreshModelsButton;

    private final AssistantType type;
    private final Predicate<ModelType> modelFilter;
    private List<CustomModel> apiModels;

    public ModelPagePanel(AssistantType type, Predicate<ModelType> modelFilter) {
        this.type = type;
        this.modelFilter = modelFilter;
        init();
        configureAvailableModels(modelFilter);
    }

    private void init() {
        if (!isApiKeyOptional()) {
            apiKeyField.getEmptyText().setText(ChatGptBundle.message("apiKey.missing", type.getFamily().getApiKeysHomepage()), SimpleTextAttributes.ERROR_ATTRIBUTES);
        }
        ItemListener proxyTypeChangedListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                enableCustomizeServerOptions(true);
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                enableCustomizeServerOptions(false);
            }
        };
        enableCustomizeUrlCheckBox.addItemListener(proxyTypeChangedListener);
        enabledInAToolCheckBox.setSelected(isEnabledInToolWindow());
        enabledInAToolCheckBox.addItemListener(this::enableInToolWindowStateChanged);
        enableCustomizeServerOptions(false);
        temperatureSpinner.setModel(new SpinnerNumberModel(0.4, 0.0, 2.0, 0.05));
        topPSpinner.setModel(new SpinnerNumberModel(0.95, 0.0, 1.0, 0.01));
        comboCombobox.setEditable(isModelNameEditable());
        comboCombobox.removeAllItems();
        testConnectionButton.addActionListener(this::testConnection);
        initHelp();
        configureAzureServerOptions(isAzureCompatible());
        if (!isModelCatalogAvailable()) {
            removeComponent(refreshModelsButton);
        }
        if (!isStreamOptionsApiAvailable()) {
            removeComponent(enableStreamOptionsCheckBox);
        }
    }

    protected boolean isModelNameEditable() {
        return false;
    }

    protected boolean isApiKeyOptional() {
        return type.getFamily().isApiKeyOptional();
    }

    protected boolean isModelCatalogAvailable() {
        return true;
    }

    protected boolean isStreamOptionsApiAvailable() {
        return false;
    }

    protected JButton createRefreshModelsButton() {
        var refreshAction = new RefreshModelsAction();
        return new JBOptionButton(refreshAction, refreshAction.getOptions());
    }

    private class RefreshModelsAction extends AbstractAction implements OptionAction {

        public RefreshModelsAction() {
            super(ChatGptBundle.message("model.list.refresh"));
        }

        @Override
        public Action @NotNull [] getOptions() {
            return new Action[] { new ResetModelsAction() };
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            var modelCatalogApi = ApplicationManager.getApplication().getService(ModelCatalogApi.class);
            var modelFamily = type.getFamily();
            var options = getAssistantOptions(GeneralSettings.getInstance());
            var baseUrl = enableCustomizeUrlCheckBox.isSelected() ?
                    customizeServerField.getText() : modelFamily.getDefaultApiEndpointUrl();

            String apiKey = apiKeyField.getPassword().length == 0 ?
                    options.getApiKey() : String.valueOf(apiKeyField.getPassword());

            if (!isApiKeyOptional() && StringUtils.isEmpty(apiKey)) {
                GUIKit.showCallout(apiKeyField, "API key is required", MessageType.ERROR);
                return;
            }

            AtomicReference<List<CustomModel>> modelsList = new AtomicReference<>();
            ParallelOptions.executeOnActionOnPooledThread(
                    (JComponent) event.getSource(),
                    () -> modelsList.set(modelCatalogApi.getAvailableModels(modelFamily, baseUrl, apiKey)),
                    () -> setAvailableModels(modelsList.get())
            );
        }
    }

    private class ResetModelsAction extends AbstractAction {

        public ResetModelsAction() {
            super(ChatGptBundle.message("model.list.reset"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            apiModels = null;
            configureAvailableModels(modelFilter);
        }
    }

    protected void enableInToolWindowStateChanged(ItemEvent e) {
        setEnabledInToolWindow(e.getStateChange() == ItemEvent.SELECTED);
    }

    public void setEnabledInToolWindow(boolean enabled) {
        if (type instanceof AssistantType.System system) {
            var enabledSet = GeneralSettings.getInstance().getEnabledInToolWindow();
            var changed = enabled ? enabledSet.add(system) : enabledSet.remove(system);

            if (changed && StartupHandler.isFullyStarted())
                ApplicationManager.getApplication().invokeLater(ChatToolWindow::synchronizeContents);
        }
    }

    public boolean isEnabledInToolWindow() {
        return type instanceof AssistantType.System system && system.isEnabled(GeneralSettings.getInstance());
    }

    public boolean isAzureCompatible() {
        return false;
    }

    protected void removeComponent(Component comp) {
        if (comp.getParent() != null)
            comp.getParent().remove(comp);
    }

    protected void configureAvailableModels(Predicate<ModelType> modelFilter) {
        comboCombobox.removeAllItems();
        StandardModel.getAvailableModels().stream()
                .filter(modelFilter)
                .forEach(model -> comboCombobox.addItem(model.id()));
    }

    protected void setAvailableModels(List<CustomModel> availableModels) {
        if (!availableModels.isEmpty()) {
            var selectedModel = comboCombobox.getSelectedItem();
            apiModels = availableModels;
            comboCombobox.removeAllItems();
            availableModels.forEach(model -> comboCombobox.addItem(model.id()));

            if (selectedModel != null) {
                comboCombobox.setSelectedItem(selectedModel);
            }
            if (comboCombobox.getSelectedIndex() == -1) {
                comboCombobox.setSelectedIndex(0);
            }
        }
    }

    protected void configureAzureServerOptions(boolean enabled) {
        if (enabled) {
            customizeServerLabel.setVisible(false);
            customizeServerOptions.setVisible(false);
            comboCombobox.setVisible(false);
            refreshModelsButton.setVisible(false);
        } else {
            removeComponent(azureApiEndpointLabel);
            removeComponent(azureApiEndpointField);
            removeComponent(azureDeploymentNameLabel);
            removeComponent(azureDeploymentNameField);
        }
    }

    private void enableCustomizeServerOptions(boolean enabled) {
        if (!enabled) {
            customizeServerField.setText(getAssistantOptions(GeneralSettings.getInstance()).getApiEndpointUrl());
        }
        UIUtil.setEnabled(customizeServerOptions, enabled, true);
    }

    protected AssistantOptions getAssistantOptions(GeneralSettings state) {
        return state.getAssistantOptions(type);
    }

    @Override
    public void reset() {
        GeneralSettings state = GeneralSettings.getInstance();
        AssistantOptions config = getAssistantOptions(state);
        setApiKeyMasked(apiKeyField, config);
        setAvailableModels(defaultIfNull(config.getApiModels(), List.of()));
        if (config.getApiModels().isEmpty()) {
            configureAvailableModels(modelFilter);
        }
        comboCombobox.setSelectedItem(config.getModelName());
        temperatureSpinner.setValue(config.getTemperature());
        topPSpinner.setValue(config.getTopP());
        enableStreamResponseCheckBox.setSelected(config.isEnableStreamResponse());
        enableStreamOptionsCheckBox.setSelected(config.isEnableStreamOptions());
        enableCustomizeUrlCheckBox.setSelected(config.isEnableCustomApiEndpointUrl());
        customizeServerField.setHistory(config.getApiEndpointUrlHistory());
        customizeServerField.setText(config.getApiEndpointUrl());
        azureApiEndpointField.setText(config.getAzureApiEndpoint());
        azureDeploymentNameField.setText(config.getAzureDeploymentName());
    }

    @Override
    public @Nullable JComponent createComponent() {
        return myMainPanel;
    }

    @Override
    public boolean isModified() {
        GeneralSettings state = GeneralSettings.getInstance();
        AssistantOptions config = getAssistantOptions(state);

        return !apiKeyField.getText().isEmpty() ||
                !config.getModelName().equals(comboCombobox.getSelectedItem()) ||
                !Double.valueOf(config.getTemperature()).equals(temperatureSpinner.getValue()) ||
                !Double.valueOf(config.getTopP()).equals(topPSpinner.getValue()) ||
                config.isEnableStreamResponse() != enableStreamResponseCheckBox.isSelected() ||
                config.isEnableStreamOptions() != enableStreamOptionsCheckBox.isSelected() ||
                config.isEnableCustomApiEndpointUrl() != enableCustomizeUrlCheckBox.isSelected() ||
                !config.getAzureApiEndpoint().equals(azureApiEndpointField.getText()) ||
                !config.getAzureDeploymentName().equals(azureDeploymentNameField.getText()) ||
                !config.getApiEndpointUrl().equals(customizeServerField.getText()) ||
                !defaultIfNull(config.getApiModels(), List.of()).equals(defaultIfNull(apiModels, List.of()));
    }

    @Override
    public void apply() {
        var settings = GeneralSettings.getInstance();
        var options = getAssistantOptions(settings);

        boolean isFirstUse = isEmpty(options.getApiKeyMasked());
        apply(options);
        maskApiKeyOnSave(options, isFirstUse);

        ChatClientHolder.refresh();
    }

    protected void apply(AssistantOptions config) {
        if (apiKeyField.getPassword().length > 0) {
            config.setApiKey(String.valueOf(apiKeyField.getPassword()));
        }
        config.setModelName(comboCombobox.getSelectedItem().toString());
        config.setTemperature((double) temperatureSpinner.getValue());
        config.setTopP((double) topPSpinner.getValue());
        config.setEnableStreamResponse(enableStreamResponseCheckBox.isSelected());
        config.setEnableStreamOptions(enableStreamOptionsCheckBox.isSelected());
        config.setEnableCustomApiEndpointUrl(enableCustomizeUrlCheckBox.isSelected());
        config.setApiEndpointUrl(customizeServerField.getText());
        config.setAzureApiEndpoint(azureApiEndpointField.getText());
        config.setAzureDeploymentName(azureDeploymentNameField.getText());
        if (!config.getApiEndpointUrlHistory().contains(config.getApiEndpointUrl()))
            customizeServerField.addCurrentTextToHistory();
        config.setApiEndpointUrlHistory(customizeServerField.getHistory());
        config.setApiModels(defaultIfNull(apiModels, List.of()));
    }

    private void maskApiKeyOnSave(AssistantOptions config, boolean isFirstUse) {
        if (apiKeyField.getPassword().length > 0) {
            setApiKeyMasked(apiKeyField, config);
            if (isFirstUse)
                setEnabledInToolWindow(true);
        }
    }

    private void setApiKeyMasked(JBPasswordField apiKeyField, AssistantOptions config) {
        apiKeyField.setText("");
        apiKeyField.getEmptyText().setText(config.getApiKeyMasked());
        if (config.getApiKeyMasked().isEmpty() && !isApiKeyOptional())
            apiKeyField.getEmptyText().setText(ChatGptBundle.message("apiKey.missing", type.getFamily().getApiKeysHomepage()), SimpleTextAttributes.ERROR_ATTRIBUTES);
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

        refreshModelsButton = createRefreshModelsButton();
        refreshModelsButton.setFocusable(true);
    }

    private void initHelp() {
        //JBFont smallFont = JBUI.Fonts.smallFont();
        //Color smallFontForeground = UIUtil.getContextHelpForeground();

        // Placeholder for future configuration options
    }

    public void testConnection(ActionEvent event) {
        var copyOfSettings = new GeneralSettings(new InMemoryCredentialStore());
        apply(getAssistantOptions(copyOfSettings));

        var actionSource = (JComponent) event.getSource();
        ParallelOptions.executeOnActionOnPooledThread(
                actionSource,
                () -> doTestConnection(actionSource, copyOfSettings)
        );
    }

    private void doTestConnection(JComponent actionSource, GeneralSettings settings) {
        ApplicationManager.getApplication().getService(ChatClientFactory.class).create(type, settings)
                .prompt()
                .user(ChatGptBundle.message("llm.test.msg"))
                .stream()
                .content()
                .collectList()
                .doOnError(cause -> {
                    SwingUtilities.invokeLater(() -> {
                        GUIKit.showCallout(actionSource, Errors.getWebClientErrorMessage(cause), MessageType.ERROR);
                    });
                })
                .doOnSuccess(resp -> {
                    String respMessage = String.join("", resp);
                    SwingUtilities.invokeLater(() -> {
                        GUIKit.showCallout(actionSource, ChatGptBundle.message("llm.test.out", respMessage), MessageType.INFO);
                    });
                })
                .doFinally(__ -> SwingUtilities.invokeLater(() -> actionSource.setEnabled(true)))
                .subscribe();
    }

    @Override
    public Configurable @NotNull [] getConfigurables() {
        return new Configurable[0];
    }
}
