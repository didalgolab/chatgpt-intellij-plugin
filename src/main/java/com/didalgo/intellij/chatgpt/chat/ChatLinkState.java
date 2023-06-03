/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.gpt3.ChatFormatDescriptor;
import com.didalgo.gpt3.GPT3Tokenizer;
import com.didalgo.gpt3.ModelType;
import com.didalgo.intellij.chatgpt.core.TextSubstitutor;
import com.didalgo.intellij.chatgpt.text.CodeFragment;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.didalgo.intellij.chatgpt.chat.ChatMessageUtils.countTokens;
import static com.didalgo.intellij.chatgpt.chat.ChatMessageUtils.isRoleSystem;

public class ChatLinkState implements ConversationContext {

    private final LinkedList<ChatMessage> chatMessages = new LinkedList<>();
    private volatile List<CodeFragment> lastSentCodeFragments = List.of();
    private volatile TextSubstitutor textSubstitutor = TextSubstitutor.NONE;
    private final ConfigurationPage configuration;


    public ChatLinkState(ConfigurationPage configuration) {
        this.configuration = configuration;
    }

    public ConfigurationPage getModelConfiguration() {
        var configuration = this.configuration;
        if (configuration == null)
            throw new UnsupportedOperationException("ModelConfiguration is not supported by this ChatLink instance");

        return configuration;
    }

    public void setTextSubstitutor(TextSubstitutor textSubstitutor) {
        this.textSubstitutor = Objects.requireNonNull(textSubstitutor);
    }

    public final TextSubstitutor getTextSubstitutor() {
        return textSubstitutor;
    }

    public Supplier<String> getSystemPrompt() {
        return getModelConfiguration().getSystemPrompt();
    }

    @Override
    public List<CodeFragment> getLastPostedCodeFragments() {
        return lastSentCodeFragments;
    }

    @Override
    public void setLastPostedCodeFragments(List<CodeFragment> codeFragments) {
        Objects.requireNonNull(codeFragments);
        this.lastSentCodeFragments = codeFragments;
    }

    @Override
    public void addChatMessage(ChatMessage message) {
        if (message.getContent() == null)
            message = new ChatMessage(message.getRole(), "");

        synchronized (chatMessages) {
            if (!chatMessages.isEmpty()) {
                if (message.getRole() == null || isRoleSystem(chatMessages.getLast()) && isRoleSystem(message)) {
                    ChatMessage last = chatMessages.removeLast();
                    message = new ChatMessage(last.getRole(), last.getContent() + message.getContent());
                }
                else if (Objects.equals(chatMessages.getLast().getRole(), message.getRole()))
                    chatMessages.removeLast();
            }
            chatMessages.add(message);
        }
    }

    @Override
    public ModelType getModelType() {
        String modelName = getModelConfiguration().getModelName();
        return ModelType.forModel(modelName).orElseThrow();
    }

    @Override
    public List<ChatMessage> getChatMessages(ModelType model, ChatMessage userMessage) {
        var chatMessages = new LinkedList<ChatMessage>();

        // First add current system message
        var systemMessage = getSystemPrompt().get();
        if (!systemMessage.isBlank())
            chatMessages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage));
        var hasSystemMessage = !chatMessages.isEmpty();

        // Add the rest of messages in the chat
        synchronized (this.chatMessages) {
            if (!this.chatMessages.isEmpty())
                chatMessages.addAll(this.chatMessages);

            // Substitute template placeholders
            substitutePlaceholders(chatMessages);

            // Trim messages if exceeding token limit
            int maxTokens = model.maxTokens();
            var tokenizer = model.getTokenizer();
            var chatFormatDescriptor = model.getChatFormatDescriptor();
            int removed = dropOldestMessagesToStayWithinTokenLimit(chatMessages, maxTokens, tokenizer, chatFormatDescriptor);
            while (removed-- > 0)
                this.chatMessages.remove(hasSystemMessage ? 1 : 0);

            return chatMessages;
        }
    }

    public void substitutePlaceholders(List<ChatMessage> chatMessages) {
        ChatMessageUtils.substitutePlaceholders(chatMessages, getTextSubstitutor());
    }

    public int dropOldestMessagesToStayWithinTokenLimit(List<ChatMessage> messages, int maxTokens, GPT3Tokenizer tokenizer, ChatFormatDescriptor formatDescriptor) {
        // here we assume ratio at most 2/3 available tokens for input prompt with context history,
        // and at least 1/3 tokens for output
        int tokenLimit = maxTokens*2/3;
        int tokenCount;
        int removed = 0;
        boolean hasSystemMessage = !messages.isEmpty() && isRoleSystem(messages.get(0));
        int oldestMessageIndex = hasSystemMessage? 1: 0;

        while ((tokenCount = countTokens(messages, tokenizer, formatDescriptor)) > tokenLimit && oldestMessageIndex < messages.size() - 1) {
            messages.remove(oldestMessageIndex);
            removed++;
        }

        if (tokenCount > tokenLimit) {
            var lastMessage = messages.get(oldestMessageIndex);
            // TODO: calculation is currently wrong
            var lastMsgCutoff = lastMessage.getContent().length() - tokenLimit;
            if (lastMsgCutoff > 0)
                messages.set(oldestMessageIndex,
                        new ChatMessage(lastMessage.getRole(), "[...] " + lastMessage.getContent().substring(lastMsgCutoff)));
        }
        return removed;
    }

    @Override
    public String getModelPage() {
        return getModelConfiguration().getModelPage();
    }

    @Override
    public void clear() {
        chatMessages.clear();
        setLastPostedCodeFragments(List.of());
    }
}
