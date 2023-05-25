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
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChatGptHandler {

    private static final Logger LOG = Logger.getInstance(ChatGptHandler.class);

    private static final String TEST_MARKDOWN = """
            Line 1
            Line 2
            
            Line 3
            
            [Selected code]
            ```java
            for (int i = 0; i < 10; i++) {
                System.out.println("2+2=5");
                System.out.println("2+2=6");
                System.out.println("2+2=7");
                System.out.println("2+2=8");
                System.out.println("2+2=9");
                System.out.println("2+2=10");
                System.out.println("2+2=11");
                System.out.println("2+2=12");
                System.out.println("2+2=13");
                System.out.println("2+2=14");
                System.out.println("2+2=15");
                System.out.println("2+2=16");
                System.out.println("2+2=17");
                System.out.println("2+2=18");
                System.out.println("2+2=19");
                System.out.println("2+2=20");
                System.out.println("2+2=21");
                System.out.println("2+2=22");
                System.out.println("2+2=23");
                System.out.println("2+2=24");
                System.out.println("2+2=25");
                System.out.println("2+2=26");
                System.out.println("2+2=27");
                System.out.println("2+2=28");
                System.out.println("2+2=29");
                System.out.println("2+2=30");
                System.out.println("2+2=31");
                System.out.println("2+2=32");
                System.out.println("2+2=33");
                System.out.println("2+2=34");
                System.out.println("2+2=35");
                System.out.println("2+2=36");
                System.out.println("2+2=37");
                System.out.println("2+2=38");
                System.out.println("2+2=39");
                System.out.println("2+2=40");
                System.out.println("2+2=41");
                System.out.println("2+2=42");
                System.out.println("2+2=43");
                System.out.println("2+2=44");
                System.out.println("2+2=45");
                System.out.println("2+2=46");
                System.out.println("2+2=47");
                System.out.println("2+2=48");
                System.out.println("2+2=49");
                System.out.println("2+2=50");
                System.out.println("2+2=51");
                System.out.println("2+2=52");
                System.out.println("2+2=53");
                System.out.println("2+2=54");
                System.out.println("2+2=55");
                System.out.println("2+2=56");
                System.out.println("2+2=57");
                System.out.println("2+2=58");
                System.out.println("2+2=59");
                System.out.println("2+2=60");
                System.out.println("2+2=61");
                System.out.println("2+2=62");
                System.out.println("2+2=63");
                System.out.println("2+2=64");
                System.out.println("2+2=65");
                System.out.println("2+2=66");
                System.out.println("2+2=67");
                System.out.println("2+2=68");
                System.out.println("2+2=69");
                System.out.println("2+2=70");
                System.out.println("2+2=71");
                System.out.println("2+2=72");
                System.out.println("2+2=73");
                System.out.println("2+2=74");
                System.out.println("2+2=75");
                System.out.println("2+2=76");
                System.out.println("2+2=77");
                System.out.println("2+2=78");
                System.out.println("2+2=79");
                System.out.println("2+2=80");
                System.out.println("2+2=81");
                System.out.println("2+2=82");
                System.out.println("2+2=83");
                System.out.println("2+2=84");
                System.out.println("2+2=85");
                System.out.println("2+2=86");
                System.out.println("2+2=87");
                System.out.println("2+2=88");
                System.out.println("2+2=89");
                System.out.println("2+2=90");
                System.out.println("2+2=91");
                System.out.println("2+2=92");
                System.out.println("2+2=93");
                System.out.println("2+2=94");
                System.out.println("2+2=95");
                System.out.println("2+2=96");
                System.out.println("2+2=97");
                System.out.println("2+2=98");
                System.out.println("2+2=99");
                System.out.println("2+2=100");
            }
            ```
            """.strip();

    private Flowable<ChatCompletionChunk> streamTestChatCompletion(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            throw new IllegalArgumentException("Hey, buddy, you can't pass a null or empty markdown. What are you trying to pull?");
        }

        return Flowable.range(0, markdown.length())
                .concatMap(index -> Flowable.just(markdown.charAt(index))
                        .map(character -> {
                            ChatCompletionChunk chunk = new ChatCompletionChunk();
                            ChatCompletionChoice choice = new ChatCompletionChoice();
                            choice.setIndex(0);
                            choice.setMessage(new ChatMessage(ChatMessageRole.ASSISTANT.value(), String.valueOf(character)));
                            chunk.setChoices(Collections.singletonList(choice));
                            return chunk;
                        })
                        .delay(10, TimeUnit.MILLISECONDS, Schedulers.computation())
                );
    }

    public Flowable<?> handle(ConversationContext ctx, ChatMessageEvent.Started event, ChatMessageListener listener) {
        var openAiService = OpenAIServiceHolder.getOpenAiService(ctx.getGroup());
        var partialResponseChoices = Collections.synchronizedSortedMap(new TreeMap<Integer, StringBuffer>());

        return openAiService.streamChatCompletion(event.getRequest().orElseThrow(() -> new IllegalArgumentException("ChatCompletionRequest is required")))
//        return streamTestChatCompletion(TEST_MARKDOWN)
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
