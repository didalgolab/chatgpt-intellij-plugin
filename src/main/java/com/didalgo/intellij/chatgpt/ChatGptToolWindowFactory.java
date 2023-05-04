/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt;

import com.didalgo.intellij.chatgpt.chat.ChatLink;
import com.didalgo.intellij.chatgpt.settings.OpenAISettingsState;
import com.didalgo.intellij.chatgpt.ui.action.tool.SettingsAction;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.*;
import com.didalgo.intellij.chatgpt.ui.BrowserContent;
import com.didalgo.intellij.chatgpt.ui.MainPanel;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ChatGptToolWindowFactory implements ToolWindowFactory {
    public enum Tab {
        GPT35_TURBO_CONTENT,
        GPT4_CONTENT,
        ONLINE_CHATGPT_CONTENT
    }

    public static final Key<Tab> ACTIVE_TAB = Key.create("didalgo.chatgpt.ToolWindow.ACTIVE_TAB");

    public static final String GPT35_TURBO_CONTENT_NAME = "GPT-3.5-Turbo";
    public static final String GPT4_CONTENT_NAME = "GPT-4";
    public static final String ONLINE_CHATGPT_CONTENT_NAME = "Online ChatGPT";
    private static final String ACTIVE_CONTENT_KEY = "didalgo.chatgpt.ToolWindow.ACTIVE";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.getInstance();
        OpenAISettingsState settings = OpenAISettingsState.getInstance();

        MainPanel gpt35TurboToolWindow = new MainPanel(project, settings.getGpt35Config());
        Content gpt35Turbo = contentFactory.createContent(gpt35TurboToolWindow.init(), GPT35_TURBO_CONTENT_NAME, false);
        gpt35Turbo.putUserData(ACTIVE_TAB, Tab.GPT35_TURBO_CONTENT);
        gpt35Turbo.setCloseable(false);

        MainPanel gpt4ToolWindow = new MainPanel(project, settings.getGpt4Config());
        Content gpt4 = contentFactory.createContent(gpt4ToolWindow.init(), GPT4_CONTENT_NAME, false);
        gpt4.putUserData(ACTIVE_TAB, Tab.GPT4_CONTENT);
        gpt4.setCloseable(false);

        BrowserContent browserToolWindow = new BrowserContent(project);
        Content browser = contentFactory.createContent(browserToolWindow.getContentPanel(), ONLINE_CHATGPT_CONTENT_NAME, false);
        browser.putUserData(ACTIVE_TAB, Tab.ONLINE_CHATGPT_CONTENT);
        browser.setCloseable(false);

        toolWindow.getContentManager().addContent(gpt35Turbo);
        toolWindow.getContentManager().addContent(gpt4);
        toolWindow.getContentManager().addContent(browser);

        // Set the default component. It require the 1st container
        Tab firstContent = Tab.valueOf(PropertiesComponent.getInstance().getValue(ACTIVE_CONTENT_KEY, Tab.GPT35_TURBO_CONTENT.name()));
        switch (firstContent) {
            case GPT35_TURBO_CONTENT    -> project.putUserData(ChatLink.KEY, gpt35TurboToolWindow.getChatLink());
            case GPT4_CONTENT           -> project.putUserData(ChatLink.KEY, gpt4ToolWindow.getChatLink());
            case ONLINE_CHATGPT_CONTENT -> project.putUserData(ChatLink.KEY, browserToolWindow.getChatLink());
        }

        // Add the selection listener
        toolWindow.addContentManagerListener(new ContentManagerListener() {
            @Override
            public void selectionChanged(@NotNull ContentManagerEvent event) {
                Tab tab = event.getContent().getUserData(ACTIVE_TAB);
                if (tab != null) {
                    switch (tab) {
                        case GPT35_TURBO_CONTENT -> project.putUserData(ChatLink.KEY, gpt35TurboToolWindow.getChatLink());
                        case GPT4_CONTENT -> project.putUserData(ChatLink.KEY, gpt4ToolWindow.getChatLink());
                        case ONLINE_CHATGPT_CONTENT -> project.putUserData(ChatLink.KEY, browserToolWindow.getChatLink());
                    }
                }
                PropertiesComponent.getInstance(project).setValue(ACTIVE_CONTENT_KEY, (tab == null)? null: tab.name());
            }
        });

        List<AnAction> actionList = new ArrayList<>();
        actionList.add(new SettingsAction(ChatGptBundle.message("action.settings")));
        toolWindow.setTitleActions(actionList);
    }
}
