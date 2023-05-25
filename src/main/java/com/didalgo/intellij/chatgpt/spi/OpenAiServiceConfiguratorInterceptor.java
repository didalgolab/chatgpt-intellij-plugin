/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.spi;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class OpenAiServiceConfiguratorInterceptor implements Interceptor {

    private final OpenAiServiceConfiguration configuration;

    public OpenAiServiceConfiguratorInterceptor(OpenAiServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        HttpUrl originalUrl = originalRequest.url();
        HttpUrl.Builder newUrlBuilder = originalUrl.newBuilder();

        List<String> pathSegments = originalUrl.pathSegments();
        Map<String, String> newPathSegments = configuration.getUrlPathSegments();
        for (int i = 0; i < pathSegments.size(); i++) {
            String pathSegment = pathSegments.get(i);
            String newPathSegment;
            if ((newPathSegment = newPathSegments.get(pathSegment)) != null) {
                newUrlBuilder.setPathSegment(i, newPathSegment);
            }
        }

        configuration.getUrlExtraQueryParameters().forEach(newUrlBuilder::addQueryParameter);

        HttpUrl newUrl = newUrlBuilder.build();
        Request request = originalRequest
                .newBuilder()
                .url(newUrl)
                .build();
        return chain.proceed(request);
    }
}
