/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.gpt3.ModelType;
import com.didalgo.intellij.chatgpt.text.TextContent;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.List;

public interface ConversationContext {

    void clear();

    String getModelPage();

    List<? extends TextContent> getLastPostedCodeFragments();

    void setLastPostedCodeFragments(List<? extends TextContent> textContents);

    void addChatMessage(ChatMessage message);

    ModelType getModelType();

    List<ChatMessage> getChatMessages(ModelType model, ChatMessage userMessage);
}
