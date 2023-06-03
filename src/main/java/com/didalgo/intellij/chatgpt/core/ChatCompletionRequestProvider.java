/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.core;

import com.didalgo.intellij.chatgpt.chat.ConversationContext;
import com.didalgo.intellij.chatgpt.settings.OpenAISettingsState;
import com.intellij.openapi.components.Service;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestBuilder;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.TreeMap;

@Service
public final class ChatCompletionRequestProvider {

    public ChatCompletionRequestBuilder chatCompletionRequest(ConversationContext ctx, ChatMessage userMessage) {
        ctx.addChatMessage(userMessage);
        var model = ctx.getModelType();
        var config = OpenAISettingsState.getInstance().getConfigurationPage(ctx.getModelPage());

        return ChatCompletionRequest
                .builder()
                .stream(config.isEnableStreamResponse())
                .temperature(config.getTemperature())
                .topP(config.getTopP())
                .model(model.modelName())
                .messages(ctx.getChatMessages(model, userMessage))
                .logitBias(new TreeMap<>());
    }
}
