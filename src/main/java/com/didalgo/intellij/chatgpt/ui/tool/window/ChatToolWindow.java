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
                for (var content : contentManager.getContents()) {
                    var assistantType = content.getUserData(ChatToolWindowFactory.ACTIVE_TAB);
                    if (assistantType != null) {
                        if (assistantTypes.contains(assistantType))
                            assistantTypesToAdd.remove(assistantType);
                        else
                            contentManager.removeContent(content, true);
                    }
                }

                if (!assistantTypesToAdd.isEmpty()) {
                    var settings = GeneralSettings.getInstance();
                    assistantTypesToAdd.forEach(type -> ChatToolWindowFactory.addToolWindowContent(toolWindow, type, settings));
                }
            }
        }
    }
}
