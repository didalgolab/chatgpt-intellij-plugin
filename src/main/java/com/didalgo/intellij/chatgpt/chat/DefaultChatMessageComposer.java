/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.intellij.chatgpt.text.CodeFragment;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import java.util.List;

public class DefaultChatMessageComposer implements ChatMessageComposer {

    @Override
    public ChatMessage compose(ConversationContext ctx, String prompt) {
        return new ChatMessage(ChatMessageRole.USER.value(), prompt);
    }

    @Override
    public ChatMessage compose(ConversationContext ctx, String prompt, List<CodeFragment> codeFragments) {
        if (codeFragments.isEmpty()) {
            return compose(ctx, prompt);
        }

        codeFragments = ChatMessageUtils.composeExcept(codeFragments, ctx.getLastPostedCodeFragments(), prompt);
        if (!codeFragments.isEmpty()) {
            ctx.setLastPostedCodeFragments(codeFragments);
            return compose(ctx, ChatMessageUtils.composeAll(prompt, codeFragments));
        }
        return compose(ctx, prompt);
    }
}
