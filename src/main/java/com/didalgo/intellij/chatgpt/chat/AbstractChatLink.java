/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.intellij.chatgpt.event.ListenerList;

public abstract class AbstractChatLink implements ChatLink {

    protected final ListenerList<ChatMessageListener> chatMessageListeners = ListenerList.of(ChatMessageListener.class);


    @Override
    public void addChatMessageListener(ChatMessageListener listener) {
        chatMessageListeners.addListener(listener);
    }

    @Override
    public void removeChatMessageListener(ChatMessageListener listener) {
        chatMessageListeners.removeListener(listener);
    }
}
