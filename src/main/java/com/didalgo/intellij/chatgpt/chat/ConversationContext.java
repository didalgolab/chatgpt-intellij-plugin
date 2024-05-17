/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.intellij.chatgpt.chat.models.ModelType;
import com.didalgo.intellij.chatgpt.text.TextContent;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;

public interface ConversationContext {

    void clear();

    AssistantType getAssistantType();

    List<? extends TextContent> getLastPostedCodeFragments();

    void setLastPostedCodeFragments(List<? extends TextContent> textContents);

    void addChatMessage(Message message);

    ModelType getModelType();

    List<Message> getChatMessages(ModelType model, UserMessage userMessage);
}
