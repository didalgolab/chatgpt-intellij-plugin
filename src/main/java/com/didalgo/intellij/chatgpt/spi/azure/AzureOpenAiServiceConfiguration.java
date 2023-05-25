/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.spi.azure;

import com.didalgo.intellij.chatgpt.spi.OpenAiServiceConfiguration;

import java.util.Map;
import java.util.Objects;

public class AzureOpenAiServiceConfiguration implements OpenAiServiceConfiguration {
    private final String deploymentId;
    private final String apiVersion;

    public AzureOpenAiServiceConfiguration(String deploymentId, String apiVersion) {
        Objects.requireNonNull(deploymentId, "deploymentId");
        Objects.requireNonNull(apiVersion, "apiVersion");
        this.deploymentId = deploymentId;
        this.apiVersion = apiVersion;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    @Override
    public Map<String, String> getUrlPathSegments() {
        return Map.of("{deployment_id}", getDeploymentId());
    }

    @Override
    public Map<String, String> getUrlExtraQueryParameters() {
        return Map.of("api-version", getApiVersion());
    }
}
