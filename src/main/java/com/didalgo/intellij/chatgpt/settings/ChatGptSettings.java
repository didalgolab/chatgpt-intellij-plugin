/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.settings;

import com.didalgo.intellij.chatgpt.chat.AssistantType;
import com.didalgo.intellij.chatgpt.chat.AssistantConfiguration;
import com.didalgo.intellij.chatgpt.chat.models.StandardModel;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.Transient;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;

import static com.didalgo.intellij.chatgpt.chat.AssistantType.System.*;

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
public class ChatGptSettings implements PersistentStateComponent<ChatGptSettings> {

    public static final String BASE_PROMPT = "You are a professional software engineer." +
            " Follow these rules in each response: snarky & noir & lang:${{LANG}}." +
            " Source code language: en. Bias towards the best solution.";

    public static final List<AssistantType.System> DEFAULT_ENABLED_SYSTEMS
            = List.of(GPT_4, GPT_3_5, AZURE_OPENAI, CLAUDE, ONLINE);

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

    private volatile List<CustomAction> customActionsPrefix = new CopyOnWriteArrayList<>();
    private volatile Set<AssistantType.System> enabledInToolWindow = new CopyOnWriteArraySet<>(DEFAULT_ENABLED_SYSTEMS);

    public String gpt35RoleText = BASE_PROMPT;

    public static ChatGptSettings getInstance() {
        return ApplicationManager.getApplication().getService(ChatGptSettings.class);
    }

    public void setGpt35Config(AssistantOptions gpt35Config) {
        this.gpt35Config = gpt35Config;
        this.gpt35Config.assistantType = AssistantType.System.GPT_3_5;
    }

    public void setGpt4Config(AssistantOptions gpt4Config) {
        this.gpt4Config = gpt4Config;
        this.gpt4Config.assistantType = GPT_4;
    }

    public void setAzureOpenAiConfig(AssistantOptions azureOpenAiConfig) {
        this.azureOpenAiConfig = azureOpenAiConfig;
        this.azureOpenAiConfig.assistantType = AssistantType.System.AZURE_OPENAI;
    }

    public void setClaudeConfig(AssistantOptions claudeConfig) {
        this.claudeConfig = claudeConfig;
        this.claudeConfig.assistantType = AssistantType.System.CLAUDE;
    }

    public void setCustomActionsPrefix(List<CustomAction> customActionsPrefix) {
        this.customActionsPrefix = new CopyOnWriteArrayList<>(customActionsPrefix);
    }

    @Getter
    @Setter
    @Tag("ApiConfig")
    public static class AssistantOptions implements AssistantConfiguration {
        private volatile AssistantType assistantType;
        private volatile String modelName;
        private volatile String apiKeyMasked = "";
        private volatile double temperature = 0.4;
        private volatile double topP = 0.95;
        @Deprecated(forRemoval = true)
        private volatile boolean enableContext = true;
        private volatile boolean enableTokenConsumption = true;
        private volatile boolean enableStreamResponse = true;
        private volatile boolean enableCustomApiEndpointUrl = false;
        private volatile String apiEndpointUrl = "";
        private volatile String azureApiEndpoint = "";
        private volatile String azureDeploymentName = "";
        private volatile List<String> apiEndpointUrlHistory = List.of(apiEndpointUrl);

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
                    enableCustomApiEndpointUrl,
                    apiEndpointUrl,
                    azureApiEndpoint,
                    azureDeploymentName
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
                        && Objects.equals(enableCustomApiEndpointUrl, that.enableCustomApiEndpointUrl)
                        && Objects.equals(azureApiEndpoint, that.azureApiEndpoint)
                        && Objects.equals(azureDeploymentName, that.azureDeploymentName)
                        && Objects.equals(apiEndpointUrl, that.apiEndpointUrl);
            }
            return false;
        }

        @Override
        @Transient
        public AssistantType getAssistantType() {
            return assistantType;
        }

        @Override
        public Supplier<String> getSystemPrompt() {
            return () -> "";
        }

        @Transient
        public String getApiKey() {
            var apiKey = PasswordSafe.getInstance().getPassword(createCredentialAttributes(getAssistantType().name()));
            if (apiKey == null)
                apiKey = "";

            return apiKey;
        }

        public void setApiKey(String apiKey) {
            var credentialAttributes = createCredentialAttributes(getAssistantType().name());
            PasswordSafe.getInstance().setPassword(credentialAttributes, apiKey);
            setApiKeyMasked(maskText(StringUtils.defaultIfEmpty(PasswordSafe.getInstance().getPassword(credentialAttributes), "")));
        }

        private static String maskText(String text) {
            final int maskStart = 3;
            final int maskEnd = 4;
            return (text.length() <= maskStart + maskEnd) ? text : text.substring(0, maskStart) + "..." + text.substring(text.length() - maskEnd);
        }

        @NotNull
        private static CredentialAttributes createCredentialAttributes(@NotNull String modelPage) {
            return new CredentialAttributes(CredentialAttributesKt.generateServiceName("com.didalgo.ChatGPT", modelPage), null);
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
            case ONLINE -> throw new IllegalArgumentException("Invalid Assistant Type: " + assistantType);
        };
    }

    @Nullable
    @Override
    public ChatGptSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ChatGptSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void reload() {
        loadState(this);
    }

    public ChatGptSettings() {
        setGpt35Config(new AssistantOptions());
        getGpt35Config().setModelName(StandardModel.GPT_3_5_TURBO.id());
        setGpt4Config(new AssistantOptions());
        getGpt4Config().setModelName(StandardModel.GPT_4O.id());
        setAzureOpenAiConfig(new AssistantOptions());
        getAzureOpenAiConfig().setModelName(StandardModel.GPT_4.id());
        setClaudeConfig(new AssistantOptions());
        getClaudeConfig().setModelName(StandardModel.CLAUDE_3_SONNET.id());
    }
}
