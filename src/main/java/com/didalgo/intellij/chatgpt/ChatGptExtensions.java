/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt;

import java.util.List;

public class ChatGptExtensions {

    public static List<ChatGptExtension> getExtensions() {
        return List.copyOf(ChatGptExtension.EP_NAME.getExtensionList());
    }


}
