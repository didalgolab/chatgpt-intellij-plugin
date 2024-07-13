/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.gpt3.ChatFormatDescriptor;
import com.didalgo.gpt3.GPT3Tokenizer;
import com.didalgo.gpt3.TokenCount;
import com.didalgo.gpt3.TokenizableFunctionCall;
import com.didalgo.gpt3.TokenizableMessage;
import com.didalgo.intellij.chatgpt.chat.messages.MessageSupport;
import com.didalgo.intellij.chatgpt.core.TextSubstitutor;
import com.didalgo.intellij.chatgpt.text.CodeFragment;
import com.didalgo.intellij.chatgpt.text.TextContent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import java.util.List;

public class ChatMessageUtils {

    public static List<TextContent> composeExcept(List<TextContent> textContents, List<? extends TextContent> exceptions, String exceptionPrompt) {
        for (var codeFragment : textContents)
            if (!exceptions.contains(codeFragment) && !exceptionPrompt.contains(TextContent.toString(codeFragment).strip()))
                return textContents;

        return List.of();
    }

    public static String composeAll(String prompt, List<? extends TextContent> textContents) {
        var buf = new StringBuilder();
        for (var textContent : textContents) {
            if (prompt.contains(textContent.toString()))
                continue;
            if (textContent instanceof CodeFragment codeFragment && StringUtils.isEmpty(codeFragment.description()))
                buf.append("[Selected code]\n");
            textContent.appendTo(buf);
            buf.append("\n\n");
        }
        if (!prompt.isEmpty()) {
            if (!buf.isEmpty())
                buf.append("---\n\n");
            buf.append(prompt);
        }

        return buf.toString();
    }

    public static boolean isRoleUser(Message chatMessage) {
        return isRole(MessageType.USER, chatMessage);
    }

    public static boolean isRoleSystem(Message chatMessage) {
        return isRole(MessageType.SYSTEM, chatMessage);
    }

    private static boolean isRole(MessageType type, Message chatMessage) {
        return type == chatMessage.getMessageType();
    }

    @SuppressWarnings("StringEquality")
    public static void substitutePlaceholders(List<Message> chatMessages, TextSubstitutor substitutor) {
        chatMessages.replaceAll(chatMessage -> {
            String template = chatMessage.getContent();
            String resolved = substitutor.resolvePlaceholders(template);
            if (resolved != template) {
                chatMessage = MessageSupport.setContent(chatMessage, resolved);
            }
            return chatMessage;
        });
    }

    public static int countTokens(List<Message> messages, GPT3Tokenizer tokenizer, ChatFormatDescriptor formatDescriptor) {
        return TokenCount.fromMessages(messages, TokenizableMessage.from(
                message -> message.getMessageType().getValue(),
                Message::getContent,
                __ -> "",
                message -> (message.getMessageType() != MessageType.TOOL)
                        ? TokenizableFunctionCall.NONE
                        : TokenizableFunctionCall.of(message.getContent(), message.getMetadata().toString())
        ), List.of(), __ -> { throw new UnsupportedOperationException("Tokenization of functions is not supported"); }, formatDescriptor, tokenizer);
    }
}
