/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.tool.window;

import com.didalgo.intellij.chatgpt.chat.AssistantType;
import com.didalgo.intellij.chatgpt.settings.ChatGptSettings;
import com.didalgo.intellij.chatgpt.ui.ChatGptPluginTestCase;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.didalgo.intellij.chatgpt.chat.AssistantType.System.CLAUDE;
import static com.didalgo.intellij.chatgpt.chat.AssistantType.System.GPT_4;
import static com.didalgo.intellij.chatgpt.chat.AssistantType.System.ONLINE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatToolWindowTest extends ChatGptPluginTestCase {

    @AfterEach
    void restoreApplicationState() {
        ChatGptSettings.getInstance().getEnabledInToolWindow().clear();
        ChatGptSettings.getInstance().getEnabledInToolWindow().addAll(ChatGptSettings.DEFAULT_ENABLED_SYSTEMS);
    }

    @Test
    public void testChatToolWindowCreation() {
        var toolWindow = registerToolWindow();

        // register content
        new ChatToolWindowFactory().createToolWindowContent(project, toolWindow);

        // verify
        var aContents = toolWindow.getContentManager().getContents();
        var expectedAssistants = ChatGptSettings.DEFAULT_ENABLED_SYSTEMS;
        assertEqualsExcept(ONLINE, expectedAssistants, Arrays.stream(aContents)
                .map(c -> c.getUserData(ChatToolWindowFactory.ACTIVE_TAB))
                .toList());
    }

    @Test
    public void testChatToolWindowCreation_onlyEnabledSystems() {
        var toolWindow = registerToolWindow();
        var ENABLED = List.of(CLAUDE);

        // register content
        ChatGptSettings.getInstance().setEnabledInToolWindow(new CopyOnWriteArraySet<>(ENABLED));
        new ChatToolWindowFactory().createToolWindowContent(project, toolWindow);

        // verify
        var aContents = toolWindow.getContentManager().getContents();
        assertEquals(ENABLED, Arrays.stream(aContents)
                .map(c -> c.getUserData(ChatToolWindowFactory.ACTIVE_TAB))
                .toList());
    }

    @Test
    public void testChatToolWindowCreation_onlyEnabledSystems_synchronizedAfterChange() {
        var toolWindow = registerToolWindow();
        var ENABLED = List.of(CLAUDE);
        var CHANGED_TO = List.of(GPT_4);

        // register content
        ChatGptSettings.getInstance().setEnabledInToolWindow(new CopyOnWriteArraySet<>(ENABLED));
        new ChatToolWindowFactory().createToolWindowContent(project, toolWindow);

        // change enabled systems
        Set<AssistantType.System> enabledInToolWindow = ChatGptSettings.getInstance().getEnabledInToolWindow();
        enabledInToolWindow.removeAll(ENABLED);
        enabledInToolWindow.addAll(CHANGED_TO);
        ChatToolWindow.synchronizeContents(enabledInToolWindow);

        // verify
        var aContents = toolWindow.getContentManager().getContents();
        assertEquals(CHANGED_TO, Arrays.stream(aContents)
                .map(c -> c.getUserData(ChatToolWindowFactory.ACTIVE_TAB))
                .toList());
    }

    static <T> void assertEqualsExcept(T except, List<? extends T> expected, List<? extends T> actual) {
        expected = new ArrayList<>(expected);
        expected.remove(except);
        actual = new ArrayList<>(actual);
        actual.remove(except);

        assertEquals(expected, actual);
    }

    protected ToolWindow registerToolWindow() {
        var toolWindowManager = (ToolWindowHeadlessManagerImpl) ToolWindowManager.getInstance(getProject());
        return toolWindowManager.doRegisterToolWindow(ChatToolWindow.TOOL_WINDOW_ID);
    }
}