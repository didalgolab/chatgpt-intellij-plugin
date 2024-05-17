/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import org.reactivestreams.Subscription;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.*;

import static java.util.Objects.requireNonNull;

public abstract class ChatMessageEvent extends EventObject {

    private final UserMessage userMessage;

    /**
     * Constructs a prototypical ChatMessageEvent.
     *
     * @param source  the object on which the Event initially occurred
     * @param userMessage the chat message associated with the event
     * @throws IllegalArgumentException if source is null
     */
    protected ChatMessageEvent(ChatLink source, UserMessage userMessage) {
        super(source);
        this.userMessage = userMessage;
    }

    /**
     * Returns the chat message associated with the event.
     *
     * @return the chat message associated with the event
     */
    public final UserMessage getUserMessage() {
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


    public static Starting starting(ChatLink source, UserMessage userMessage) {
        return new Starting(source, userMessage);
    }


    public static class Starting extends ChatMessageEvent {

        protected Starting(ChatLink source, UserMessage userMessage) {
            super(source, userMessage);
        }

        protected Starting(Starting sourceEvent) {
            this(sourceEvent.getChatLink(), sourceEvent.getUserMessage());
        }

        public Started started(Subscription subscription) {
            return new Started(this, subscription);
        }

        public Initiating initiating(Prompt prompt) {
            return new Initiating(this, prompt);
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
        private final Prompt prompt;

        protected Initiating(Starting sourceEvent, Prompt prompt) {
            super(sourceEvent);
            this.prompt = prompt;
        }

        public final Optional<Prompt> getPrompt() {
            return Optional.ofNullable(prompt);
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

        public ResponseArriving responseArriving(ChatResponse responseChunk, List<Generation> partialResponseChoices) {
            requireNonNull(responseChunk, "responseChunk");
            requireNonNull(partialResponseChoices, "partialResponseChoices");
            return new ResponseArriving(this, responseChunk, partialResponseChoices);
        }

        public ResponseArrived responseArrived(List<Generation> responseChoices) {
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
        private final ChatResponse responseChunk;
        private final List<Generation> partialResponseChoices;

        protected ResponseArriving(Started sourceEvent, ChatResponse responseChunk, List<Generation> partialResponseChoices) {
            super(sourceEvent);
            this.responseChunk = responseChunk;
            this.partialResponseChoices = partialResponseChoices;
        }

        public final ChatResponse getResponseChunk() {
            return responseChunk;
        }

        public final List<Generation> getPartialResponseChoices() {
            return partialResponseChoices;
        }
    }

    public static class ResponseArrived extends Started {
        private final List<Generation> responseChoices;

        protected ResponseArrived(Started sourceEvent, List<Generation> responseChoices) {
            super(sourceEvent);
            this.responseChoices = responseChoices;
        }

        public final List<Generation> getResponseChoices() {
            return responseChoices;
        }
    }
}
