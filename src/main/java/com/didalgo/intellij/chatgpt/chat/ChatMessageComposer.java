/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.intellij.chatgpt.text.CodeFragment;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.List;

public interface ChatMessageComposer {

    ChatMessage compose(ConversationContext ctx, String prompt);

    ChatMessage compose(ConversationContext ctx, String prompt, List<CodeFragment> codeFragments);

}
