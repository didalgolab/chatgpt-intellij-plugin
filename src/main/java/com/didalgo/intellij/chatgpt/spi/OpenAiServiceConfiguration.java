/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.spi;

import java.util.Map;

public interface OpenAiServiceConfiguration {

    Map<String, String> getUrlPathSegments();

    Map<String, String> getUrlExtraQueryParameters();
}
