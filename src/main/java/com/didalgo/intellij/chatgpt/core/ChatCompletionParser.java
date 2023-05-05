/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.core;

import com.didalgo.intellij.chatgpt.text.TextFragment;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.List;

public class ChatCompletionParser {

    public static TextFragment parseGPT35TurboWithStream(List<ChatMessage> assistantResponses) {
        String assistantMessage = assistantResponses.isEmpty() ? null : assistantResponses.get(0).getContent();
        if (assistantMessage == null) {
            assistantMessage = "";
        }

        TextFragment parseResult = TextFragment.of(assistantMessage);
        parseResult.toHtml(); // pre-compute and cache HTML content in the current thread
        return parseResult;
    }

    public static class ParseResult {
        private String markdown;
        private String html;

        public String getMarkdown() {
            return markdown;
        }

        public String getHtml() {
            return html;
        }
    }

}
