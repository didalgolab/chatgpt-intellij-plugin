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
            ```properties
            # Gradle Releases -> https://github.com/gradle/gradle/releases
            gradleVersion = 7.6
            
            # IntelliJ Platform Artifacts Repositories
            # -> https://www.jetbrains.org/intellij/sdk/docs/reference_guide/intellij_artifacts.html
            pluginGroup = com.didalgo.chatgpt
            pluginName = ChatGPT Tools
            pluginSinceBuild = 222.2680.4
            pluginUntilBuild = 231.*
            pluginVersion = 0.1.15-222.231
            
            # IntelliJ Platform Properties -> https://github.com/JetBrains/gradle-intellij-plugin#intellij-platform-properties
            # 'IC' - IntelliJ IDEA Community Edition.
            # 'IU' - IntelliJ IDEA Ultimate Edition.
            # 'CL' - CLion.
            # 'PY' - PyCharm Professional Edition.
            # 'PC' - PyCharm Community Edition.
            # 'RD' - Rider.
            # 'GO' - GoLand.
            # 'JPS' - JPS-only.
            platformDownloadSources = true
            platformPlugins = org.intellij.plugins.markdown
            platformType = IC
            platformVersion = 2023.1
            
            # Opt-out flag for bundling Kotlin standard library.
            # See https://plugins.jetbrains.com/docs/intellij/kotlin.html#kotlin-standard-library for details.
            # suppress inspection "UnusedProperty"
            kotlin.stdlib.default.dependency = false
            
            # Java language level used to compile sources and to generate the files for - Java 11 is required since 2020.3
            javaVersion = 17
            
            org.gradle.jvmargs = '-Dfile.encoding=UTF-8'
            ```
            """.repeat(1).strip();

    private Flowable<ChatCompletionChunk> streamTestChatCompletion(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            throw new IllegalArgumentException("Hej, ziomeczku, nie możesz przekazać pustego lub nullowego markdownu. Co próbujesz osiągnąć?");
        }

        return Flowable.range(0, markdown.length())
                .buffer(5)
                .concatMap(indices -> {
                    int startIndex = indices.get(0);
                    int endIndex = indices.get(indices.size() - 1) + 1;
                    String chunk = markdown.substring(startIndex, endIndex);
                    ChatCompletionChunk completionChunk = new ChatCompletionChunk();
                    List<ChatCompletionChoice> choices = new ArrayList<>();
                        ChatCompletionChoice choice = new ChatCompletionChoice();
                        choice.setIndex(0);
                        choice.setMessage(new ChatMessage(ChatMessageRole.ASSISTANT.value(), chunk));
                        choices.add(choice);
                    completionChunk.setChoices(choices);
                    return Flowable.just(completionChunk)
                            .delay(25, TimeUnit.MILLISECONDS, Schedulers.computation())
                            .observeOn(Schedulers.computation());
                })
                .observeOn(Schedulers.computation());
    }

    public Flowable<?> handle(ConversationContext ctx, ChatMessageEvent.Started event, ChatMessageListener listener) {
        var openAiService = OpenAIServiceHolder.getOpenAiService(ctx.getModelPage());
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
