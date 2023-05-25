/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt;

import com.intellij.openapi.extensions.ExtensionPointName;

public interface ChatGptExtension {

    ExtensionPointName<ChatGptExtension> EP_NAME = ExtensionPointName.create("com.didalgo.chatgpt.chatGptExtension");

    boolean isEnabled(String prompt);
}
