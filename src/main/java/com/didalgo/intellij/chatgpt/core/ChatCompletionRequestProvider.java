/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.core;

import com.didalgo.intellij.chatgpt.chat.ConversationContext;
import com.intellij.openapi.components.Service;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

@Service
public final class ChatCompletionRequestProvider {

    public Prompt chatCompletionRequest(ConversationContext ctx, UserMessage userMessage) {
        ctx.addChatMessage(userMessage);
        return new Prompt(ctx.getChatMessages(ctx.getModelType(), userMessage));
    }
}
