/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

public interface ChatLinkProvider {
    /**
     * Returns the ChatLink object associated with this provider.
     *
     * @return The ChatLink object
     */
    ChatLink getChatLink();
}