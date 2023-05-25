/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.spi;

import com.didalgo.intellij.chatgpt.settings.OpenAISettingsState;
import com.didalgo.intellij.chatgpt.text.encryption.AES;
import com.theokanning.openai.service.OpenAiService;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class DefaultOpenAiServiceProvider implements OpenAiServiceProvider {

    @Override
    public boolean supportsEndpoint(String url) {
        return true;
    }

    @Override
    public OpenAiService createService(String group, OpenAISettingsState settings) {
        var modelSettings = settings.getConfigForCategory(group);

        return new OpenAiService(AES.decrypt(modelSettings.getApiKey()),
                Duration.of(Long.parseLong(settings.getReadTimeout()), ChronoUnit.MILLIS));
    }
}
