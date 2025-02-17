/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.settings;

import com.didalgo.intellij.chatgpt.chat.AssistantType;
import com.didalgo.intellij.chatgpt.chat.AssistantConfiguration;
import com.didalgo.intellij.chatgpt.chat.models.CustomModel;
import com.didalgo.intellij.chatgpt.chat.models.ModelType;
import com.didalgo.intellij.chatgpt.chat.models.StandardModel;
import com.didalgo.intellij.chatgpt.settings.auth.CredentialStore;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.serviceContainer.NonInjectable;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.System;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;

import static com.didalgo.intellij.chatgpt.chat.AssistantType.System.*;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

/**
 * Supports storing the application settings in a persistent way.
 * <p>
 * The {@link State} and {@link Storage} annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 */
@Getter
@Setter
@State(
        name = "settings.com.didalgo.intellij.chatgpt.OpenAISettingsState",
        storages = @Storage("didalgo-intellij-chatgpt-settings.xml")
)
@Tag("ChatGptSettings") // for backward compatibility
public class GeneralSettings implements PersistentStateComponent<GeneralSettings> {

    public static final String BASE_PROMPT = "You are a professional software engineer." +
            " Follow these rules in each response: lang:${{LANG}}." +
            " Source code language: en. Bias towards the best solution.";

    public static final List<AssistantType.System> DEFAULT_ENABLED_SYSTEMS
            = List.of(GPT_4, GPT_3_5);

    public Map<Integer, String> contentOrder = new HashMap<>() {{
        put(1, AssistantType.System.GPT_3_5.displayName());
        put(2, AssistantType.System.ONLINE.displayName());
    }};

    private volatile String readTimeout = "50000";
    private volatile boolean enableAvatar = true;
    private volatile boolean enableLineWarp = true;
    private volatile Boolean enableInitialMessage = null;

    private volatile AssistantOptions gpt35Config;
    private volatile AssistantOptions gpt4Config;
    private volatile AssistantOptions azureOpenAiConfig;
    private volatile AssistantOptions claudeConfig;
    private volatile AssistantOptions geminiConfig;
    private volatile AssistantOptions ollamaConfig;

    private volatile List<CustomAction> customActionsPrefix = new CopyOnWriteArrayList<>();
    private volatile Set<AssistantType.System> enabledInToolWindow = new CopyOnWriteArraySet<>(DEFAULT_ENABLED_SYSTEMS);

    public String gpt35RoleText = BASE_PROMPT;

    public static GeneralSettings getInstance() {
        return ApplicationManager.getApplication().getService(GeneralSettings.class);
    }

    public GeneralSettings() {
        this((CredentialStore) null);
    }

    @NonInjectable
    protected GeneralSettings(CredentialStore credStore) {
        setGpt35Config(AssistantOptions.forAssistantType(GPT_3_5, credStore, StandardModel.GPT_4_O_MINI.id()));
        setGpt4Config(AssistantOptions.forAssistantType(GPT_4, credStore, StandardModel.GPT_4_O.id()));
        setAzureOpenAiConfig(AssistantOptions.forAssistantType(AZURE_OPENAI, credStore, StandardModel.GPT_4.id()));
        setClaudeConfig(AssistantOptions.forAssistantType(CLAUDE, credStore));
        setGeminiConfig(AssistantOptions.forAssistantType(GEMINI, credStore));
        setOllamaConfig(AssistantOptions.forAssistantType(OLLAMA, credStore, "llama3"));
    }

    public void setGpt35Config(AssistantOptions gpt35Config) {
        this.gpt35Config = gpt35Config;
        this.gpt35Config.setAssistantType(GPT_3_5);
    }

    public void setGpt4Config(AssistantOptions gpt4Config) {
        this.gpt4Config = gpt4Config;
        this.gpt4Config.setAssistantType(GPT_4);
    }

    public void setAzureOpenAiConfig(AssistantOptions azureOpenAiConfig) {
        this.azureOpenAiConfig = azureOpenAiConfig;
        this.azureOpenAiConfig.setAssistantType(AssistantType.System.AZURE_OPENAI);
    }

    public void setClaudeConfig(AssistantOptions claudeConfig) {
        this.claudeConfig = claudeConfig;
        this.claudeConfig.setAssistantType(AssistantType.System.CLAUDE);
    }

    public void setGeminiConfig(AssistantOptions geminiConfig) {
        this.geminiConfig = geminiConfig;
        this.geminiConfig.setAssistantType(AssistantType.System.GEMINI);
    }

    public void setOllamaConfig(AssistantOptions ollamaConfig) {
        this.ollamaConfig = ollamaConfig;
        this.ollamaConfig.setAssistantType(AssistantType.System.OLLAMA);
    }

    public void setCustomActionsPrefix(List<CustomAction> customActionsPrefix) {
        this.customActionsPrefix = new CopyOnWriteArrayList<>(customActionsPrefix);
    }

    public Set<AssistantType.System> getEnabledInToolWindow() {
        enabledInToolWindow.removeIf(Objects::isNull);
        return enabledInToolWindow;
    }

    @Getter
    @Setter
    @Tag("ApiConfig") // for backward compatibility
    public static class AssistantOptions implements AssistantConfiguration {
        private final @Getter(AccessLevel.NONE) CredentialStore credentialStore;
        private volatile AssistantType assistantType;
        private volatile String modelName;
        private volatile String apiKeyMasked = "";
        private volatile double temperature = 0.4;
        private volatile double topP = 0.95;
        @Deprecated(forRemoval = true)
        private volatile boolean enableContext = true;
        private volatile boolean enableTokenConsumption = true;
        private volatile boolean enableStreamResponse = true;
        private volatile boolean enableStreamOptions = true;
        private volatile boolean enableCustomApiEndpointUrl = false;
        private volatile String apiEndpointUrl = "";
        private volatile String azureApiEndpoint = "";
        private volatile String azureDeploymentName = "";
        private volatile List<String> apiEndpointUrlHistory = List.of(apiEndpointUrl);
        private volatile List<CustomModel> apiModels = List.of();

        public AssistantOptions() {
            this((CredentialStore) null);
        }

        @NonInjectable
        private AssistantOptions(CredentialStore credStore) {
            this.credentialStore = credStore;
        }

        public static AssistantOptions forAssistantType(AssistantType assistantType, CredentialStore credStore) {
            var firstModel = StandardModel.findFirstAvailableModelInFamily(assistantType.getFamily())
                    .map(ModelType::id).orElse(null);

            return forAssistantType(assistantType, credStore, firstModel);
        }

        public static AssistantOptions forAssistantType(AssistantType assistantType, CredentialStore credStore, String modelName) {
            var options = new AssistantOptions(credStore);
            options.setAssistantType(assistantType);
            options.setApiEndpointUrl(assistantType.getFamily().getDefaultApiEndpointUrl());
            options.setModelName(modelName);

            return options;
        }

        public void setAssistantType(AssistantType assistantType) {
            this.assistantType = assistantType;
            if ("".equals(getApiEndpointUrl())) {
                setApiEndpointUrl(assistantType.getFamily().getDefaultApiEndpointUrl());
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    assistantType.name(),
                    modelName,
                    apiKeyMasked,
                    temperature,
                    topP,
                    enableTokenConsumption,
                    enableStreamResponse,
                    enableStreamOptions,
                    enableCustomApiEndpointUrl,
                    apiEndpointUrl,
                    azureApiEndpoint,
                    azureDeploymentName,
                    defaultIfNull(apiModels, List.of())
            );
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AssistantOptions that) {
                return Objects.equals(assistantType, that.assistantType)
                        && Objects.equals(modelName, that.modelName)
                        && Objects.equals(apiKeyMasked, that.apiKeyMasked)
                        && Objects.equals(temperature, that.temperature)
                        && Objects.equals(topP, that.topP)
                        && Objects.equals(enableTokenConsumption, that.enableTokenConsumption)
                        && Objects.equals(enableStreamResponse, that.enableStreamResponse)
                        && Objects.equals(enableStreamOptions, that.enableStreamOptions)
                        && Objects.equals(enableCustomApiEndpointUrl, that.enableCustomApiEndpointUrl)
                        && Objects.equals(apiEndpointUrl, that.apiEndpointUrl)
                        && Objects.equals(azureApiEndpoint, that.azureApiEndpoint)
                        && Objects.equals(azureDeploymentName, that.azureDeploymentName)
                        && Objects.equals(defaultIfNull(apiModels, List.of()), defaultIfNull(that.apiModels, List.of()));
            }
            return false;
        }

        @Override
        @Transient
        public AssistantType getAssistantType() {
            return assistantType;
        }

        @Transient
        public ModelType getModelType() {
            String modelName = getModelName();
            try {
                return StandardModel.of(modelName);
            } catch (IllegalArgumentException e) {
                var customModels = getApiModels();
                if (customModels != null)
                    for (var model : customModels)
                        if (modelName.equals(model.id()))
                            return model;

                return new CustomModel(modelName, assistantType.getFamily(), Integer.MAX_VALUE);
            }
        }

        @Override
        public Supplier<String> getSystemPrompt() {
            return () -> "";
        }

        private CredentialStore credentialStore() {
            return (credentialStore != null) ? credentialStore : CredentialStore.systemCredentialStore();
        }

        @Transient
        public String getApiKey() {
            CredentialStore currStore = credentialStore(), systemStore = CredentialStore.systemCredentialStore();

            var apiKey = currStore.getPassword(getAssistantType().name());
            if (apiKey == null && currStore != systemStore) {
                apiKey = systemStore.getPassword(getAssistantType().name());
            }
            if (apiKey == null) {
                apiKey = "";
            }

            return apiKey;
        }

        public void setApiKey(String apiKey) {
            setApiKeyMasked(maskText(defaultIfEmpty(
                    credentialStore().setAndGetPassword(getAssistantType().name(), apiKey), "")));
        }

        private static String maskText(String text) {
            final int maskStart = 3;
            final int maskEnd = 4;
            return (text.length() <= maskStart + maskEnd) ? text : text.substring(0, maskStart) + "..." + text.substring(text.length() - maskEnd);
        }
    }

    @Transient
    public AssistantOptions getAssistantOptions(AssistantType assistantType) {
        if (!(assistantType instanceof AssistantType.System system))
            throw new IllegalArgumentException("Invalid Assistant Type: " + assistantType);

        return switch (system) {
            case GPT_3_5 -> gpt35Config;
            case GPT_4 -> gpt4Config;
            case AZURE_OPENAI -> azureOpenAiConfig;
            case CLAUDE -> claudeConfig;
            case GEMINI -> geminiConfig;
            case OLLAMA -> ollamaConfig;
            case ONLINE -> throw new IllegalArgumentException("Invalid Assistant Type: " + assistantType);
        };
    }

    @Nullable
    @Override
    public GeneralSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull GeneralSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void reload() {
        loadState(this);
    }
}
