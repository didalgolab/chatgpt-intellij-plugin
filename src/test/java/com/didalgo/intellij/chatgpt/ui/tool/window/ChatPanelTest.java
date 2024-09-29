/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.tool.window;

import com.didalgo.intellij.chatgpt.chat.AssistantType;
import com.didalgo.intellij.chatgpt.chat.client.ChatClientFactory;
import com.didalgo.intellij.chatgpt.settings.GeneralSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.impl.ApplicationImpl;
import com.intellij.openapi.extensions.DefaultPluginDescriptor;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.testFramework.junit5.TestApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.Mockito.*;

@TestApplication
class ChatPanelTest {

    private ChatModel chatModel;

    @BeforeEach
    void setUp() {
        chatModel = mockChatModel(ChatModel.class);
    }

    @ParameterizedTest
    @EnumSource(value = AssistantType.System.class, mode = EXCLUDE, names = "ONLINE")
    void can_converse_with_api(AssistantType.System type) throws Throwable {
        when(chatModel.stream(any(Prompt.class)))
                .thenReturn(Flux.just(
                        new ChatResponse(List.of(new Generation("some"))),
                        new ChatResponse(List.of(new Generation("thing")))));

        var chatPanel = aChatPanel(type);
        aUserMessage(chatPanel, "Say something");

        verifyEventually(() -> {
            assertEquals("something", chatPanel.getConversationTurnPanel(-1).getMessageText().markdown());
            assertEquals("Say something", chatPanel.getConversationTurnPanel(-2).getMessageText().markdown());
        });
    }

    @TestApplication
    @Nested
    class NonStreaming {
        private ChatModel nonStreamingChatModel;

        @BeforeEach
        void setUp() {
            nonStreamingChatModel = mockChatModel(ChatModel.class);
        }

        @ParameterizedTest
        @EnumSource(value = AssistantType.System.class, mode = EXCLUDE, names = "ONLINE")
        void can_converse_with_non_streaming_api_too(AssistantType.System type) throws Throwable {
            when(nonStreamingChatModel.stream(any(Prompt.class))).thenThrow(UnsupportedOperationException.class);
            when(nonStreamingChatModel.call(any(Prompt.class)))
                    .thenReturn(new ChatResponse(List.of(new Generation("something"))));

            var chatPanel = aChatPanel(type);
            aUserMessage(chatPanel, "Say something");

            verifyEventually(() -> {
                assertEquals("something", chatPanel.getConversationTurnPanel(-1).getMessageText().markdown());
                assertEquals("Say something", chatPanel.getConversationTurnPanel(-2).getMessageText().markdown());
            });
        }
    }

    protected static void verifyEventually(Runnable action) throws Throwable {
        Instant until = Instant.now().plusSeconds(5);
        boolean successful = false;
        do {
            try {
                try {
                    SwingUtilities.invokeAndWait(action);
                    successful = true;
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            } catch (AssertionFailedError e) {
                if (Instant.now().isAfter(until)) {
                    throw e;
                }
                Thread.sleep(5L);
            }
        } while (!successful);
    }

    @SuppressWarnings("UnstableApiUsage")
    static <T extends ChatModel> T mockChatModel(Class<T> clazz) {
        var chatModel = Mockito.mock(clazz);
        var application = (ApplicationImpl) ApplicationManager.getApplication();
        application.registerServiceInstance(
                ChatClientFactory.class,
                new ChatClientFactory() {
                    @Override
                    public ChatClient create(AssistantType type, GeneralSettings settings) {
                        return ChatClient.create(chatModel);
                    }
                },
                new DefaultPluginDescriptor("com.didalgo.chatgpt"));

        return chatModel;
    }

    static ChatPanel aChatPanel(AssistantType type) {
        var project = ProjectManager.getInstance().getDefaultProject();
        var setings = GeneralSettings.getInstance().getAssistantOptions(type);

        return new ChatPanel(project, setings) {
            @Override
            protected boolean presetCheck() { return true; }
        };
    }

    static void aUserMessage(ChatPanel chatPanel, String message) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            chatPanel.setSearchText(message);
            chatPanel.getSubmitButton().doClick(0);
        });
    }
}