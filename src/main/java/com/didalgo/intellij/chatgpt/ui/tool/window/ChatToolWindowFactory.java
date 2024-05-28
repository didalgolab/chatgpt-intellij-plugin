/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.tool.window;

import com.didalgo.intellij.chatgpt.ChatGptBundle;
import com.didalgo.intellij.chatgpt.chat.AssistantType;
import com.didalgo.intellij.chatgpt.chat.ChatLink;
import com.didalgo.intellij.chatgpt.chat.ChatLinkProvider;
import com.didalgo.intellij.chatgpt.settings.GeneralSettings;
import com.didalgo.intellij.chatgpt.ui.action.tool.SettingsAction;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.*;
import com.didalgo.intellij.chatgpt.ui.BrowserContent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatToolWindowFactory implements ToolWindowFactory, DumbAware {

    public static final Key<AssistantType> ACTIVE_TAB = Key.create("didalgo.ChatToolWindow.ACTIVE_TAB");

    private static final Key<Map<AssistantType, AssistantTool>> CONTENT_MAP = Key.create("didalgo.ChatToolWindow.CONTENT_MAP");
    private static final String ACTIVE_CONTENT_KEY = "didalgo.chatgpt.ToolWindow.ACTIVE";
    private static final Logger log = Logger.getInstance(ChatToolWindowFactory.class);


    private static void setContentMap(ToolWindow toolWindow, Map<AssistantType, AssistantTool> tools) {
        toolWindow.getProject().putUserData(CONTENT_MAP, tools);
    }

    private static Map<AssistantType, AssistantTool> getContentMap(ToolWindow toolWindow) {
        return toolWindow.getProject().getUserData(CONTENT_MAP);
    }

    public static void addToolWindowContent(ToolWindow toolWindow, AssistantType type, GeneralSettings settings) {
        var contentFactory = new AssistantToolFactory(toolWindow.getProject(), settings, ContentFactory.getInstance());
        var contentMap = getContentMap(toolWindow);

        addToolWindowContent(toolWindow, type, contentFactory, contentMap);
    }

    private static void addToolWindowContent(
            ToolWindow toolWindow,
            AssistantType type,
            AssistantToolFactory contentFactory,
            Map<AssistantType, AssistantTool> contentMap)
    {
        try {
            AssistantTool assistant = contentFactory.createAssistantTool(type);
            toolWindow.getContentManager().addContent(assistant.content());
            contentMap.put(type, assistant);
        }
        catch (Exception | Error e) {
            if (type == AssistantType.System.ONLINE)
                log.warn("'ChatGPT Online' is disabled due to: " + e.getMessage());
            else
                throw e;
        }
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        GeneralSettings settings = GeneralSettings.getInstance();
        AssistantToolFactory contentFactory = new AssistantToolFactory(project, settings, ContentFactory.getInstance());

        Map<AssistantType, AssistantTool> contentMap = new ConcurrentHashMap<>();
        for (var type : AssistantType.System.values()) {
            if (!type.isEnabled(settings))
                continue;

            addToolWindowContent(toolWindow, type, contentFactory, contentMap);
        }
        setContentMap(toolWindow, contentMap);

        // Set the default component. It require the 1st container
        String activeContent = PropertiesComponent.getInstance().getValue(ACTIVE_CONTENT_KEY, AssistantType.System.GPT_4.name());
        try {
            AssistantType.System activeContentKey = AssistantType.System.valueOf(activeContent);
            var content = contentMap.get(activeContentKey);
            if (content != null) {
                project.putUserData(ChatLink.KEY, content.getChatLink());
            }
        } catch (Exception ignored) { }

        // Add the selection listener
        toolWindow.addContentManagerListener(new ContentManagerListener() {
            @Override
            public void selectionChanged(@NotNull ContentManagerEvent event) {
                var assistantType = event.getContent().getUserData(ACTIVE_TAB);
                if (assistantType instanceof AssistantType.System system) {
                    var content = contentMap.get(system);
                    if (content != null) {
                        project.putUserData(ChatLink.KEY, content.getChatLink());
                    }
                }
                PropertiesComponent.getInstance(project).setValue(ACTIVE_CONTENT_KEY, (assistantType == null)? null: assistantType.name());
            }
        });

        List<AnAction> actionList = new ArrayList<>();
        actionList.add(new SettingsAction(ChatGptBundle.message("action.settings")) {
            @Override
            public Class<? extends Configurable> getPanelToSelect() {
                Content content;
                var contentManager = toolWindow.getContentManagerIfCreated();
                if (contentManager != null && (content = contentManager.getSelectedContent()) != null) {
                    var assistantType = content.getUserData(ACTIVE_TAB);
                    if (assistantType != null)
                        return assistantType.getConfigurable();
                }
                return super.getPanelToSelect();
            }
        });
        toolWindow.setTitleActions(actionList);
    }

    private static class AssistantToolFactory {
        private final Project project;
        private final GeneralSettings settings;
        private final ContentFactory contentFactory;

        AssistantToolFactory(Project project, GeneralSettings settings, ContentFactory contentFactory) {
            this.project = project;
            this.settings = settings;
            this.contentFactory = contentFactory;
        }

        AssistantTool createAssistantTool(AssistantType type) {
            ChatLinkProvider provider;
            Content content;
            if (type == AssistantType.System.ONLINE) {
                BrowserContent browser = new BrowserContent(project);
                content = contentFactory.createContent(browser.getContentPanel(), type.displayName(), false);
                provider = browser;
            } else {
                ChatPanel chatPanel = new ChatPanel(project, settings.getAssistantOptions(type));
                content = contentFactory.createContent(chatPanel.init(), type.displayName(), false);
                provider = chatPanel;
            }
            content.putUserData(ACTIVE_TAB, type);
            content.setCloseable(false);

            return new AssistantTool(provider, content);
        }
    }

    record AssistantTool(ChatLinkProvider provider, Content content) {

        public ChatLink getChatLink() {
            return provider.getChatLink();
        }
    }
}
