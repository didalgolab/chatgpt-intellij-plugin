/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.core;

import com.didalgo.intellij.chatgpt.util.HtmlUtil;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.List;

public class ChatCompletionParser {

    public static ParseResult parseGPT35TurboWithStream(List<ChatMessage> assistantResponses) {
        String assistantMessage = assistantResponses.isEmpty() ? null : assistantResponses.get(0).getContent();
        if (assistantMessage == null) {
            assistantMessage = "";
        }

        ParseResult parseResult = new ParseResult();
        parseResult.source = assistantMessage;
        parseResult.html = HtmlUtil.md2html(parseResult.source);
        return parseResult;
    }

    public static class ParseResult {
        private String source;
        private String html;

        public String getSource() {
            return source;
        }

        public String getHtml() {
            return html;
        }
    }

}
