/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.client;

import com.didalgo.intellij.chatgpt.chat.ChatMessageEvent;
import com.didalgo.intellij.chatgpt.chat.ChatMessageListener;
import com.didalgo.intellij.chatgpt.chat.ConversationContext;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Subscription;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Consumer;

public class ChatHandler {

    private static final Logger LOG = Logger.getInstance(ChatHandler.class);

    public Flux<?> handle(ConversationContext ctx, ChatMessageEvent.Initiating event, ChatMessageListener listener) {
        var chatClient = ChatClientHolder.getChatClient(ctx.getAssistantType());
        var flowHandler = new ChatCompletionHandler(listener);
        var prompt = event.getPrompt().orElseThrow(() -> new IllegalArgumentException("Prompt is required"));

        try {
            return chatClient.prompt(prompt).stream().chatResponse()
                    .doOnSubscribe(flowHandler.onSubscribe(event))
                    .doOnError(flowHandler.onError())
                    .doOnComplete(flowHandler.onComplete(ctx))
                    .doOnNext(flowHandler.onNextChunk());
        }
        catch (UnsupportedOperationException e) {
            return Mono.fromCallable(() -> chatClient.prompt(prompt).call().chatResponse())
                    .flux()
                    .doOnSubscribe(flowHandler.onSubscribe(event))
                    .doOnError(flowHandler.onError())
                    .doOnComplete(flowHandler.onComplete(ctx))
                    .doOnNext(flowHandler.onNext());
        }
    }

    static class ChatCompletionHandler {
        private final ChatMessageListener listener;
        private final SortedMap<Integer, StringBuffer> partialResponseChoices;
        private volatile ChatMessageEvent.Started event;

        public ChatCompletionHandler(ChatMessageListener listener) {
            this.listener = listener;
            this.partialResponseChoices = Collections.synchronizedSortedMap(new TreeMap<>());
        }

        public Consumer<Subscription> onSubscribe(ChatMessageEvent.Initiating event) {
            return subscription -> {
                listener.exchangeStarted(this.event = event.started(subscription));
            };
        }

        public Runnable onComplete(ConversationContext ctx) {
            return () -> {
                var assistantMessages = toMessages(partialResponseChoices);
                if (!assistantMessages.isEmpty()) {
                    ctx.addChatMessage(assistantMessages.get(0).getOutput());
                }
                listener.responseArrived(event.responseArrived(assistantMessages));
            };
        }

        public Consumer<ChatResponse> onNextChunk() {
            return chunk -> {
                if (chunk.getResult() != null) {
                    listener.responseArriving(event.responseArriving(chunk, formResponse(chunk.getResult())));
                }
            };
        }

        public Consumer<ChatResponse> onNext() {
            return result -> {
                if (result.getResult() != null) {
                    listener.responseArrived(event.responseArrived(formResponse(result.getResult())));
                }
            };
        }

        public Consumer<Throwable> onError() {
            return cause -> {
                listener.exchangeFailed(event.failed(cause));
                cause.printStackTrace();
            };
        }

        private List<Generation> formResponse(Generation choice) {
            partialResponseChoices.computeIfAbsent(0, __ -> new StringBuffer())
                    .append(StringUtils.defaultIfEmpty(choice.getOutput().getContent(), ""));
            return toMessages(partialResponseChoices);
        }

        private List<Generation> toMessages(SortedMap<Integer, StringBuffer> partialResponseChoices) {
            List<AssistantMessage> responseChoices = new ArrayList<>(partialResponseChoices.size());
            partialResponseChoices.forEach((key, value) -> responseChoices.add(new AssistantMessage(value.toString())));
            return List.of(new Generation(responseChoices.get(0).getContent()));
        }
    }
}
