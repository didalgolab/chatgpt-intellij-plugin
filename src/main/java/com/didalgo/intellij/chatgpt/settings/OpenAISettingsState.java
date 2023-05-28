/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.settings;

import com.didalgo.gpt3.ModelType;
import com.didalgo.intellij.chatgpt.ChatGptToolWindowFactory;
import com.didalgo.intellij.chatgpt.ModelPage;
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

    private volatile OpenAIConfig gpt35Config;
    private volatile OpenAIConfig gpt4Config;
    @Transient
    private volatile String activePage = ModelPage.GPT_3_5.name();


    private volatile List<CustomAction> customActionsPrefix = new CopyOnWriteArrayList<>();

    public String gpt35RoleText = "You are an expert in software development. From now on follow these rules: ['snarky', 'noir']";

    public static OpenAISettingsState getInstance() {
        return ApplicationManager.getApplication().getService(OpenAISettingsState.class);
    }

    public void setGpt35Config(OpenAIConfig gpt35Config) {
        gpt35Config.facetName = ModelPage.GPT_3_5.name();
        this.gpt35Config = gpt35Config;
    }

    public void setGpt4Config(OpenAIConfig gpt4Config) {
        gpt4Config.facetName = ModelPage.GPT_4.name();
        this.gpt4Config = gpt4Config;
    }

    public void setCustomActionsPrefix(List<CustomAction> customActionsPrefix) {
        this.customActionsPrefix = new CopyOnWriteArrayList<>(customActionsPrefix);
    }

    @Getter
    @Setter
    @Tag("ApiConfig")
    public static class OpenAIConfig implements ChatLinkStateConfiguration {
        private volatile String facetName;
        private volatile String apiKey = Optional.ofNullable(System.getenv("OPENAI_API_KEY")).orElse("");
        private volatile String modelName;
        private volatile boolean enableContext = true;
        private volatile boolean enableTokenConsumption = true;
        private volatile boolean enableStreamResponse = true;
        private volatile boolean enableCustomApiEndpointUrl = false;
        private volatile String apiEndpointUrl = DEFAULT_API_ENDPOINT;
        private volatile List<String> apiEndpointUrlHistory = List.of(apiEndpointUrl);

        private static final String DEFAULT_API_ENDPOINT = "https://api.openai.com/v1/chat/completions";

        @Override
        public int hashCode() {
            return Objects.hash(facetName, apiKey, modelName, enableContext, enableTokenConsumption,
                    enableStreamResponse, enableCustomApiEndpointUrl, apiEndpointUrl);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof OpenAIConfig that) {
                return Objects.equals(facetName, that.facetName)
                        && Objects.equals(apiKey, that.apiKey)
                        && Objects.equals(modelName, that.modelName)
                        && Objects.equals(enableContext, that.enableContext)
                        && Objects.equals(enableTokenConsumption, that.enableTokenConsumption)
                        && Objects.equals(enableStreamResponse, that.enableStreamResponse)
                        && Objects.equals(enableCustomApiEndpointUrl, that.enableCustomApiEndpointUrl)
                        && Objects.equals(apiEndpointUrl, that.apiEndpointUrl);
            }
            return false;
        }

        @Transient
        public final String getFacetName() {
            return facetName;
        }

        private void setFacetName(String facetName) {
            this.facetName = facetName;
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
    public OpenAIConfig getConfigForPage(String page) {
        return switch (page) {
            case ModelPage.Of.GPT_3_5 -> gpt35Config;
            case ModelPage.Of.GPT_4 -> gpt4Config;
            default -> throw new IllegalArgumentException("Invalid Model Page: " + page);
        };
    }

    @Transient
    public OpenAIConfig getActiveConfig() {
        return getConfigForPage(activePage);
    }

    public void setActivePage(String page) {
        this.activePage = page;
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
        setGpt35Config(new OpenAIConfig());
        getGpt35Config().setModelName(ModelType.GPT_3_5_TURBO.modelName());
        setGpt4Config(new OpenAIConfig());
        getGpt4Config().setModelName(ModelType.GPT_4.modelName());
    }
}
