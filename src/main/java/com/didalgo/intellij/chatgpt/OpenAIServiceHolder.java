/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt;

import com.didalgo.intellij.chatgpt.spi.OpenAiServiceFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OpenAIServiceHolder {

    private static final Map<String, OpenAiService> openAiServices = new ConcurrentHashMap<>();

    public static synchronized OpenAiService getOpenAiService(String category) {
        return openAiServices.computeIfAbsent(category, __ -> createOpenAiService(category));
    }

    public static synchronized void refresh() {
        List<OpenAiService> services = new ArrayList<>(openAiServices.values());
        openAiServices.clear();
        services.forEach(OpenAiService::shutdownExecutor);
    }

    protected static OpenAiService createOpenAiService(String group) {
        return ApplicationManager.getApplication().getService(OpenAiServiceFactory.class)
                .create(group);
    }
}
