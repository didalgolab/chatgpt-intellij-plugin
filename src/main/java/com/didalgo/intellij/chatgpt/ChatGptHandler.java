/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt;

import com.didalgo.intellij.chatgpt.chat.ChatMessageEvent;
import com.didalgo.intellij.chatgpt.chat.ChatMessageListener;
import com.didalgo.intellij.chatgpt.chat.ConversationContext;
import com.intellij.openapi.diagnostic.Logger;
import com.theokanning.openai.completion.chat.*;
import io.reactivex.Flowable;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ChatGptHandler {

    private static final Logger LOG = Logger.getInstance(ChatGptHandler.class);

    public Flowable<?> handle(ConversationContext ctx, ChatMessageEvent.Started event, ChatMessageListener listener) {
        var openAiService = OpenAIServiceHolder.getOpenAiService(ctx.getGroup());
        var partialResponseChoices = Collections.synchronizedSortedMap(new TreeMap<Integer, StringBuffer>());

        return openAiService.streamChatCompletion(event.getRequest().orElseThrow(() -> new IllegalArgumentException("ChatCompletionRequest is required")))
                .doOnSubscribe(subscription -> listener.exchangeStarted(event.started(subscription)))
                .doOnError(t -> listener.exchangeFailed(event.failed(t)))
                .doOnComplete(() -> {
                    var assistantMessages = toResponseChoices(partialResponseChoices);
                    if (!assistantMessages.isEmpty()) {
                        ctx.addChatMessage(assistantMessages.get(0));
                    }
                    listener.responseArrived(event.responseArrived(assistantMessages));
                })
                .doOnNext(chunk -> {
                    if (chunk.getChoices().isEmpty()) {
                        return;
                    }
                    chunk.getChoices().forEach(choice -> {
                        partialResponseChoices.computeIfAbsent(choice.getIndex(), __ -> new StringBuffer())
                                .append(StringUtils.defaultIfEmpty(choice.getMessage().getContent(), ""));
                    });
                    listener.responseArriving(
                            event.responseArriving(chunk,
                                    toResponseChoices(partialResponseChoices)));
                });
    }

    protected List<ChatMessage> toResponseChoices(SortedMap<Integer, StringBuffer> partialResponseChoices) {
        List<ChatMessage> responseChoices = new ArrayList<>(partialResponseChoices.size());
        partialResponseChoices.forEach((key, value) -> responseChoices.add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), value.toString())));
        return responseChoices;
    }
}
