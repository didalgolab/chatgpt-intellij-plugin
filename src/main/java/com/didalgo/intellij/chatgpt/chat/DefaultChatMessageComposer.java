/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.intellij.chatgpt.text.TextContent;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import java.util.List;

public class DefaultChatMessageComposer implements ChatMessageComposer {

    @Override
    public ChatMessage compose(ConversationContext ctx, String prompt) {
        return new ChatMessage(ChatMessageRole.USER.value(), prompt);
    }

    @Override
    public ChatMessage compose(ConversationContext ctx, String prompt, List<? extends TextContent> textContents) {
        if (textContents.isEmpty()) {
            return compose(ctx, prompt);
        }

        textContents = ChatMessageUtils.composeExcept(textContents, ctx.getLastPostedCodeFragments(), prompt);
        if (!textContents.isEmpty()) {
            ctx.setLastPostedCodeFragments(textContents);
            return compose(ctx, ChatMessageUtils.composeAll(prompt, textContents));
        }
        return compose(ctx, prompt);
    }
}
