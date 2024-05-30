/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.tool.window;

import com.didalgo.intellij.chatgpt.chat.AssistantType;
import com.didalgo.intellij.chatgpt.settings.GeneralSettings;
import com.didalgo.intellij.chatgpt.ui.ChatGptPluginTestCase;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.didalgo.intellij.chatgpt.chat.AssistantType.System.CLAUDE;
import static com.didalgo.intellij.chatgpt.chat.AssistantType.System.GPT_3_5;
import static com.didalgo.intellij.chatgpt.chat.AssistantType.System.GPT_4;
import static com.didalgo.intellij.chatgpt.chat.AssistantType.System.ONLINE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatToolWindowTest extends ChatGptPluginTestCase {

    @AfterEach
    void restoreApplicationState() {
        GeneralSettings.getInstance().getEnabledInToolWindow().clear();
        GeneralSettings.getInstance().getEnabledInToolWindow().addAll(GeneralSettings.DEFAULT_ENABLED_SYSTEMS);
    }

    @Test
    void factory_creates_some_chat_assistants_by_default() {
        var toolWindow = registerToolWindow();

        // register content
        new ChatToolWindowFactory().createToolWindowContent(project, toolWindow);

        // verify
        var aContents = toolWindow.getContentManager().getContents();
        var expectedAssistants = GeneralSettings.DEFAULT_ENABLED_SYSTEMS;
        assertEqualsExcept(ONLINE, expectedAssistants, Arrays.stream(aContents)
                .map(c -> c.getUserData(ChatToolWindowFactory.ACTIVE_TAB))
                .toList());
    }

    @Test
    void factory_creates_only_chat_assistants_enabled_in_settings() {
        var toolWindow = registerToolWindow();
        var ENABLED = List.of(CLAUDE);

        // register content
        GeneralSettings.getInstance().setEnabledInToolWindow(new CopyOnWriteArraySet<>(ENABLED));
        new ChatToolWindowFactory().createToolWindowContent(project, toolWindow);

        // verify
        var aContents = toolWindow.getContentManager().getContents();
        assertEquals(ENABLED, Arrays.stream(aContents)
                .map(c -> c.getUserData(ChatToolWindowFactory.ACTIVE_TAB))
                .toList());
    }

    @Test
    void factory_synchronizes_visible_chat_assistants_when_changed_in_settings() {
        var toolWindow = registerToolWindow();
        var ENABLED = List.of(GPT_4, GPT_3_5);
        var CHANGED_TO = List.of(GPT_4, CLAUDE);

        // register content
        GeneralSettings.getInstance().setEnabledInToolWindow(new CopyOnWriteArraySet<>(ENABLED));
        new ChatToolWindowFactory().createToolWindowContent(project, toolWindow);

        // change enabled systems
        scenario.disable(GPT_3_5);
        scenario.enable(CLAUDE);

        // verify
        var aContentManager = toolWindow.getContentManager();
        var aContents = aContentManager.getContents();
        assertEquals(CHANGED_TO, Arrays.stream(aContents)
                .map(c -> c.getUserData(ChatToolWindowFactory.ACTIVE_TAB))
                .toList());
        assertEquals(CLAUDE, aContentManager.getSelectedContent().getUserData(ChatToolWindowFactory.ACTIVE_TAB),
                "The last enabled Tool Window content should become selected");
    }

    @Test
    void selected_chat_assistant_does_not_change_after_disabling_another() {
        var toolWindow = registerToolWindow();
        var ENABLED = List.of(GPT_4, GPT_3_5, CLAUDE);

        // register content
        GeneralSettings.getInstance().setEnabledInToolWindow(new CopyOnWriteArraySet<>(ENABLED));
        new ChatToolWindowFactory().createToolWindowContent(project, toolWindow);
        var SELECTED = toolWindow.getContentManager().getSelectedContent().getUserData(ChatToolWindowFactory.ACTIVE_TAB);

        // change enabled systems
        scenario.disable(GPT_3_5);

        // verify
        var aContentManager = toolWindow.getContentManager();
        assertEquals(SELECTED, aContentManager.getSelectedContent().getUserData(ChatToolWindowFactory.ACTIVE_TAB));
    }

    static final class Scenario {

        void enable(AssistantType.System... chatSystems) {
            var enabledInToolWindow = GeneralSettings.getInstance().getEnabledInToolWindow();
            enabledInToolWindow.addAll(List.of(chatSystems));
            ChatToolWindow.synchronizeContents(enabledInToolWindow);
        }

        void disable(AssistantType.System... chatSystems) {
            var enabledInToolWindow = GeneralSettings.getInstance().getEnabledInToolWindow();
            enabledInToolWindow.removeAll(List.of(chatSystems));
            ChatToolWindow.synchronizeContents(enabledInToolWindow);
        }
    }
    Scenario scenario = new Scenario();

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