/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.tool.window;

import com.didalgo.intellij.chatgpt.chat.AssistantType;
import com.didalgo.intellij.chatgpt.settings.GeneralSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;

import java.util.ArrayList;
import java.util.Set;

public class ChatToolWindow {

    public static final String TOOL_WINDOW_ID = "com.didalgo.ChatGPT";

    public static ToolWindow locate(Project project) {
        return ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
    }

    public static void ensureActivated(Project project) {
        var chatGPT = locate(project);
        if (chatGPT == null) {
            throw new AssertionError("Unable to find " + TOOL_WINDOW_ID + " Tool Window");
        }
        if (!chatGPT.isActive()) {
            chatGPT.activate(null);
        }
    }

    public static void synchronizeContents() {
        synchronizeContents(GeneralSettings.getInstance().getEnabledInToolWindow());
    }

    public static void synchronizeContents(Set<? extends AssistantType> assistantTypes) {
        var openProjects = ProjectManager.getInstance().getOpenProjects();

        for (Project project : openProjects) {
            var assistantTypesToAdd = new ArrayList<>(assistantTypes);
            var toolWindow = locate(project);
            var contentManager = toolWindow.getContentManagerIfCreated();

            if (contentManager != null) {
                removeDisabledChatSystems(assistantTypes, contentManager, assistantTypesToAdd);
                if (!assistantTypesToAdd.isEmpty()) {
                    addEnabledChatSystems(assistantTypesToAdd, toolWindow, contentManager);
                }
            }
        }
    }

    private static void removeDisabledChatSystems(Set<? extends AssistantType> assistantTypes, ContentManager contentManager, ArrayList<? extends AssistantType> assistantTypesToAdd) {
        for (var content : contentManager.getContents()) {
            var assistantType = content.getUserData(ChatToolWindowFactory.ACTIVE_TAB);
            if (assistantType != null) {
                if (assistantTypes.contains(assistantType))
                    assistantTypesToAdd.remove(assistantType);
                else
                    contentManager.removeContent(content, true);
            }
        }
    }

    private static void addEnabledChatSystems(ArrayList<? extends AssistantType> assistantTypesToAdd, ToolWindow toolWindow, ContentManager contentManager) {
        var settings = GeneralSettings.getInstance();
        assistantTypesToAdd.forEach(type -> ChatToolWindowFactory.addToolWindowContent(toolWindow, type, settings));

        Content[] contents = contentManager.getContents();
        if (contents.length > 0) {
            contentManager.setSelectedContent(contents[contents.length - 1]);
        }
    }
}
