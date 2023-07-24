/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.spi;

import com.didalgo.intellij.chatgpt.settings.OpenAISettingsState;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class DefaultOpenAiServiceProvider implements OpenAiServiceProvider {

    @Override
    public boolean supportsEndpoint(String url) {
        return true;
    }

    @Override
    public OpenAiService createService(String page, OpenAISettingsState settings) {
        var modelSettings = settings.getConfigurationPage(page);

        String baseUrl = modelSettings.getApiEndpointUrl();
        if (baseUrl.endsWith("v1/chat/completions") || baseUrl.endsWith("v1/chat/completions/")) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("v1/chat/completions"));
        }

        OkHttpClient client = OpenAiService.defaultClient(modelSettings.getApiKey(),
                Duration.of(Long.parseLong(settings.getReadTimeout()), ChronoUnit.MILLIS));
        ObjectMapper mapper = OpenAiService.defaultObjectMapper();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        OpenAiApi api = retrofit.create(OpenAiApi.class);
        return new OpenAiService(api, client.dispatcher().executorService());
    }
}
