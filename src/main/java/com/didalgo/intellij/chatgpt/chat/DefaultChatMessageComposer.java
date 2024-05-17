/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.intellij.chatgpt.text.TextContent;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;

public class DefaultChatMessageComposer implements ChatMessageComposer {

    @Override
    public UserMessage compose(ConversationContext ctx, String textContent, List<Media> mediaList) {
        return new UserMessage(textContent, mediaList);
    }

    @Override
    public UserMessage compose(ConversationContext ctx, String userPrompt, List<TextContent> textContents, List<Media> mediaList) {
        if (textContents.isEmpty()) {
            return compose(ctx, userPrompt, mediaList);
        }

        textContents = ChatMessageUtils.composeExcept(textContents, ctx.getLastPostedCodeFragments(), userPrompt);
        if (!textContents.isEmpty()) {
            ctx.setLastPostedCodeFragments(textContents);
            return compose(ctx, ChatMessageUtils.composeAll(userPrompt, textContents), mediaList);
        }
        return compose(ctx, userPrompt, mediaList);
    }
}
