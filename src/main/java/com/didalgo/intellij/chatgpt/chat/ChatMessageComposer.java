/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.intellij.chatgpt.text.TextContent;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.Media;

import java.util.List;

public interface ChatMessageComposer {

    UserMessage compose(ConversationContext ctx, String textContent, List<Media> mediaList);

    UserMessage compose(ConversationContext ctx, String userPrompt, List<TextContent> textContents, List<Media> mediaList);

}
