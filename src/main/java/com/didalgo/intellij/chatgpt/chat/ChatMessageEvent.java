/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import org.reactivestreams.Subscription;

import java.util.*;

import static java.util.Objects.requireNonNull;

public abstract class ChatMessageEvent extends EventObject {

    private final ChatMessage userMessage;

    /**
     * Constructs a prototypical ChatMessageEvent.
     *
     * @param source  the object on which the Event initially occurred
     * @param userMessage the chat message associated with the event
     * @throws IllegalArgumentException if source is null
     */
    protected ChatMessageEvent(ChatLink source, ChatMessage userMessage) {
        super(source);
        this.userMessage = userMessage;
    }

    /**
     * Returns the chat message associated with the event.
     *
     * @return the chat message associated with the event
     */
    public final ChatMessage getUserMessage() {
        return userMessage;
    }

    /**
     * Returns the ChatLink object on which the event initially occurred.
     *
     * @return the ChatLink object
     */
    public final ChatLink getChatLink() {
        return (ChatLink) getSource();
    }


    public static Starting starting(ChatLink source, ChatMessage userMessage) {
        return new Starting(source, userMessage);
    }


    public static class Starting extends ChatMessageEvent {

        protected Starting(ChatLink source, ChatMessage userMessage) {
            super(source, userMessage);
        }

        protected Starting(Starting sourceEvent) {
            this(sourceEvent.getChatLink(), sourceEvent.getUserMessage());
        }

        public Started started(Subscription subscription) {
            return new Started(this, subscription);
        }

        public Initiating initiating(ChatCompletionRequest request) {
            return new Initiating(this, request);
        }

        public Failed failed(Throwable cause) {
            requireNonNull(cause, "cause");
            return new Failed(this, cause);
        }

        public Cancelled cancelled() {
            return new Cancelled(this);
        }
    }

    public static class Initiating extends Starting {
        private final ChatCompletionRequest request;

        protected Initiating(Starting sourceEvent, ChatCompletionRequest request) {
            super(sourceEvent);
            this.request = request;
        }

        public final Optional<ChatCompletionRequest> getRequest() {
            return Optional.ofNullable(request);
        }
    }

    public static class Started extends Starting {
        private volatile Subscription subscription;

        protected Started(Started sourceEvent) {
            this(sourceEvent, sourceEvent.getSubscription());
        }

        protected Started(Starting sourceEvent, Subscription subscription) {
            super(sourceEvent);
            this.subscription = subscription;
        }

        public final Subscription getSubscription() {
            return subscription;
        }

        public ResponseArriving responseArriving(ChatCompletionChunk responseChunk, List<ChatMessage> partialResponseChoices) {
            requireNonNull(responseChunk, "responseChunk");
            requireNonNull(partialResponseChoices, "partialResponseChoices");
            return new ResponseArriving(this, responseChunk, partialResponseChoices);
        }

        public ResponseArrived responseArrived(List<ChatMessage> responseChoices) {
            requireNonNull(responseChoices, "responseChoices");
            return new ResponseArrived(this, responseChoices);
        }
    }

    public static class Failed extends Starting {
        private final Throwable cause;

        protected Failed(Starting sourceEvent, Throwable cause) {
            super(sourceEvent);
            this.cause = cause;
        }

        public final Throwable getCause() {
            return cause;
        }
    }

    public static class Cancelled extends Starting {
        protected Cancelled(Starting sourceEvent) {
            super(sourceEvent);
        }
    }

    public static class ResponseArriving extends Started {
        private final ChatCompletionChunk responseChunk;
        private final List<ChatMessage> partialResponseChoices;

        protected ResponseArriving(Started sourceEvent, ChatCompletionChunk responseChunk, List<ChatMessage> partialResponseChoices) {
            super(sourceEvent);
            this.responseChunk = responseChunk;
            this.partialResponseChoices = partialResponseChoices;
        }

        public final ChatCompletionChunk getResponseChunk() {
            return responseChunk;
        }

        public final List<ChatMessage> getPartialResponseChoices() {
            return partialResponseChoices;
        }
    }

    public static class ResponseArrived extends Started {
        private final List<ChatMessage> responseChoices;

        protected ResponseArrived(Started sourceEvent, List<ChatMessage> responseChoices) {
            super(sourceEvent);
            this.responseChoices = responseChoices;
        }

        public final List<ChatMessage> getResponseChoices() {
            return responseChoices;
        }
    }
}
