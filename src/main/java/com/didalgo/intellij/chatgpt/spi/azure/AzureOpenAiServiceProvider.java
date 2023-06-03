/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.spi.azure;

import com.didalgo.intellij.chatgpt.settings.OpenAISettingsState;
import com.didalgo.intellij.chatgpt.spi.OpenAiServiceConfiguratorInterceptor;
import com.didalgo.intellij.chatgpt.spi.OpenAiServiceProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AzureOpenAiServiceProvider implements OpenAiServiceProvider {

//    private static final Pattern URL_PATTERN
    private static final String BASE_DOMAIN = "openai.azure.com";

    private static final Pattern BASE_URL_PATTERN = Pattern.compile("(https?://[^/]+/)");
    private static final Pattern DEPLOYMENT_ID_PATTERN = Pattern.compile("/deployments/([^/]+)");
    private static final Pattern API_VERSION_PATTERN = Pattern.compile("[?&]api-version=([^&]+)");

    @Override
    public boolean supportsEndpoint(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost().endsWith(BASE_DOMAIN);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    @Override
    public OpenAiService createService(String group, OpenAISettingsState settings) {
        var modelSettings = settings.getConfigurationPage(group);
        var completionUrl = modelSettings.getApiEndpointUrl();
        var deploymentId = extractDeploymentId(completionUrl);
        var apiVersion = extractApiVersion(completionUrl);
        var baseUrl = extractBaseUrl(completionUrl);
        var timeout = Duration.of(Long.parseLong(settings.getReadTimeout()), ChronoUnit.MILLIS);
        var token = modelSettings.getApiKey();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        AzureOpenAiServiceConfiguration azureConfig = new AzureOpenAiServiceConfiguration(deploymentId, apiVersion);
        ObjectMapper mapper = OpenAiService.defaultObjectMapper();
        OkHttpClient client = OpenAiService.defaultClient(token, timeout)
                .newBuilder()
                //.addInterceptor(loggingInterceptor)
                .addInterceptor(new OpenAiServiceConfiguratorInterceptor(azureConfig))
                .addInterceptor(new AzureAuthenticationInterceptor(token))
                .build();

        client.newBuilder();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        AzureOpenAiApi api = retrofit.create(AzureOpenAiApi.class);
        ExecutorService executorService = client.dispatcher().executorService();
        return new OpenAiService(api, executorService);
    }

    private static String extractBaseUrl(String url) {
        Matcher matcher = BASE_URL_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String extractDeploymentId(String url) {
        Matcher matcher = DEPLOYMENT_ID_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String extractApiVersion(String url) {
        Matcher matcher = API_VERSION_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : "";
    }
}
