/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import reactor.core.Disposable;

@FunctionalInterface
public interface ConversationHandler {

    Disposable push(ConversationContext ctx, ChatMessageEvent.Starting event, ChatMessageListener listener);

}
