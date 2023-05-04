/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.settings;

import com.didalgo.gpt3.ModelType;
import com.didalgo.intellij.chatgpt.ChatGptToolWindowFactory;
import com.didalgo.intellij.chatgpt.ModelCategory;
import com.didalgo.intellij.chatgpt.chat.ChatLinkStateConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.Transient;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

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
public class OpenAISettingsState implements PersistentStateComponent<OpenAISettingsState> {

    public Map<Integer,String> contentOrder = new HashMap<>() {{
        put(1, ChatGptToolWindowFactory.GPT35_TURBO_CONTENT_NAME);
        put(2, ChatGptToolWindowFactory.ONLINE_CHATGPT_CONTENT_NAME);
    }};

    private volatile String readTimeout = "50000";
    private volatile boolean enableAvatar = true;
    private volatile boolean enableLineWarp = true;

    private volatile OpenAIConfig gpt35Config = new OpenAIConfig();
    private volatile OpenAIConfig gpt4Config = new OpenAIConfig();
    @Transient
    private volatile String currentConfigID = ModelCategory.GPT_3_5;


    private volatile List<CustomAction> customActionsPrefix = new CopyOnWriteArrayList<>();

    public String gpt35RoleText = "You are an expert in software development. From now on follow these rules: ['snarky', 'noir']";

    public static OpenAISettingsState getInstance() {
        return ApplicationManager.getApplication().getService(OpenAISettingsState.class);
    }

    public void setGpt35Config(OpenAIConfig gpt35Config) {
        gpt35Config.group = ModelCategory.GPT_3_5;
        this.gpt35Config = gpt35Config;
    }

    public void setGpt4Config(OpenAIConfig gpt4Config) {
        gpt4Config.group = ModelCategory.GPT_4;
        this.gpt4Config = gpt4Config;
    }

    public void setCustomActionsPrefix(List<CustomAction> customActionsPrefix) {
        this.customActionsPrefix = new CopyOnWriteArrayList<>(customActionsPrefix);
    }

    @Getter
    @Setter
    @Tag("ApiConfig")
    public static class OpenAIConfig implements ChatLinkStateConfiguration {
        private volatile String group;
        private volatile String apiKey = Optional.ofNullable(System.getenv("OPENAI_API_KEY")).orElse("");
        private volatile String modelName;
        private volatile boolean enableContext = true;
        private volatile boolean enableTokenConsumption = true;
        private volatile boolean enableGPT35StreamResponse = true;
        private volatile boolean enableCustomizeGpt35TurboUrl = false;
        private volatile String gpt35TurboUrl = "https://api.openai.com/v1/chat/completions";

        @Override
        public int hashCode() {
            return Objects.hash(group, apiKey, modelName, enableContext, enableTokenConsumption,
                    enableGPT35StreamResponse, enableCustomizeGpt35TurboUrl, gpt35TurboUrl);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof OpenAIConfig that) {
                return Objects.equals(group, that.group)
                        && Objects.equals(apiKey, that.apiKey)
                        && Objects.equals(modelName, that.modelName)
                        && Objects.equals(enableContext, that.enableContext)
                        && Objects.equals(enableTokenConsumption, that.enableTokenConsumption)
                        && Objects.equals(enableGPT35StreamResponse, that.enableGPT35StreamResponse)
                        && Objects.equals(enableCustomizeGpt35TurboUrl, that.enableCustomizeGpt35TurboUrl)
                        && Objects.equals(gpt35TurboUrl, that.gpt35TurboUrl);
            }
            return false;
        }

        @Override
        @Transient
        public final String getGroup() {
            return group;
        }

        private void setGroup(String group) {
            this.group = group;
        }

        @Override
        public Supplier<String> getSystemPrompt() {
            return () -> "";
        }
    }

    public OpenAIConfig getGpt35Config() {
        return gpt35Config;
    }

    public OpenAIConfig getGpt4Config() {
        return gpt4Config;
    }

    @Transient
    public OpenAIConfig getConfigForCategory(String category) {
        return switch (category) {
            case ModelCategory.GPT_3_5 -> gpt35Config;
            case ModelCategory.GPT_4 -> gpt4Config;
            default -> throw new IllegalArgumentException("Invalid category: " + category);
        };
    }

    @Transient
    public OpenAIConfig getCurrentConfig() {
        return getConfigForCategory(currentConfigID);
    }

    public void setCurrentConfigID(String id) {
        this.currentConfigID = id;
    }

    @Nullable
    @Override
    public OpenAISettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull OpenAISettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void reload() {
        loadState(this);
    }

    public OpenAISettingsState() {
        gpt35Config.setModelName(ModelType.GPT_3_5_TURBO.modelName());
        gpt4Config.setModelName(ModelType.GPT_4.modelName());
    }
}
