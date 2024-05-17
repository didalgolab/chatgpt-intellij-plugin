/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.core;

import com.didalgo.intellij.chatgpt.text.TextFragment;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.List;

public class ChatCompletionParser {

    public static TextFragment parseTextContent(Generation generation) {
        AssistantMessage assistantMessage = generation.getOutput();
        if (assistantMessage == null) {
            assistantMessage = new AssistantMessage("");
        }

        TextFragment parseResult = TextFragment.of(assistantMessage.getContent());
        parseResult.toHtml(); // pre-compute and cache HTML content in the current thread
        return parseResult;
    }
}
