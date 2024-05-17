/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.tool.window;

import com.didalgo.intellij.chatgpt.ChatGptBundle;
import com.didalgo.intellij.chatgpt.chat.AssistantType;
import com.didalgo.intellij.chatgpt.chat.ChatLink;
import com.didalgo.intellij.chatgpt.chat.ChatLinkProvider;
import com.didalgo.intellij.chatgpt.settings.ChatGptSettings;
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

public class ChatServiceToolWindowFactory implements ToolWindowFactory, DumbAware {

    public static final Key<AssistantType> ACTIVE_TAB = Key.create("didalgo.ChatServiceToolWindow.ACTIVE_TAB");

    private static final String ACTIVE_CONTENT_KEY = "didalgo.chatgpt.ToolWindow.ACTIVE";
    private static final Logger log = Logger.getInstance(ChatServiceToolWindowFactory.class);


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        AssistantContentFactory contentFactory = new AssistantContentFactory(project, ChatGptSettings.getInstance(), ContentFactory.getInstance());

        AssistantContent gpt35Turbo = contentFactory.createAssistantContent(AssistantType.System.GPT_3_5);
        AssistantContent gpt4 = contentFactory.createAssistantContent(AssistantType.System.GPT_4);
        AssistantContent azureOpenAi = contentFactory.createAssistantContent(AssistantType.System.AZURE_OPENAI);
        AssistantContent claude = contentFactory.createAssistantContent(AssistantType.System.CLAUDE);

        toolWindow.getContentManager().addContent(gpt4.content());
        toolWindow.getContentManager().addContent(gpt35Turbo.content());
        toolWindow.getContentManager().addContent(azureOpenAi.content());
        toolWindow.getContentManager().addContent(claude.content());

        AssistantContent browser = null;
        try {
            browser = contentFactory.createJcefBrowserContent();
            toolWindow.getContentManager().addContent(browser.content);
        } catch (IllegalStateException e) {
            log.warn("'ChatGPT Online' is disabled due to: " + e.getMessage());
        }

        // Set the default component. It require the 1st container
        String activeContent = PropertiesComponent.getInstance().getValue(ACTIVE_CONTENT_KEY, AssistantType.System.GPT_4.name());
        try {
            AssistantType.System activeContentKey = AssistantType.System.valueOf(activeContent);
            switch (activeContentKey) {
                case GPT_3_5 -> project.putUserData(ChatLink.KEY, gpt35Turbo.getChatLink());
                case GPT_4 -> project.putUserData(ChatLink.KEY, gpt4.getChatLink());
                case AZURE_OPENAI -> project.putUserData(ChatLink.KEY, azureOpenAi.getChatLink());
                case CLAUDE -> project.putUserData(ChatLink.KEY, claude.getChatLink());
                case ONLINE ->
                        Optional.ofNullable(browser).ifPresent(win -> project.putUserData(ChatLink.KEY, win.getChatLink()));
            }
        } catch (Exception ignored) { }

        // Add the selection listener
        var _browserToolWindow = browser;
        toolWindow.addContentManagerListener(new ContentManagerListener() {
            @Override
            public void selectionChanged(@NotNull ContentManagerEvent event) {
                var assistantType = event.getContent().getUserData(ACTIVE_TAB);
                if (assistantType instanceof AssistantType.System system) {
                    switch (system) {
                        case GPT_3_5 -> project.putUserData(ChatLink.KEY, gpt35Turbo.getChatLink());
                        case GPT_4   -> project.putUserData(ChatLink.KEY, gpt4.getChatLink());
                        case AZURE_OPENAI -> project.putUserData(ChatLink.KEY, azureOpenAi.getChatLink());
                        case CLAUDE  -> project.putUserData(ChatLink.KEY, claude.getChatLink());
                        case ONLINE  -> Optional.ofNullable(_browserToolWindow).ifPresent(win -> project.putUserData(ChatLink.KEY, win.getChatLink()));
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

    private static class AssistantContentFactory {
        private final Project project;
        private final ChatGptSettings settings;
        private final ContentFactory contentFactory;

        AssistantContentFactory(Project project, ChatGptSettings settings, ContentFactory contentFactory) {
            this.project = project;
            this.settings = settings;
            this.contentFactory = contentFactory;
        }

        AssistantContent createAssistantContent(AssistantType type) {
            var chatPanel = new ChatPanel(project, settings.getAssistantOptions(type));
            var content = contentFactory.createContent(chatPanel.init(), type.displayName(), false);
            content.putUserData(ACTIVE_TAB, type);
            content.setCloseable(false);

            return new AssistantContent(chatPanel, content);
        }

        AssistantContent createJcefBrowserContent() {
            var assistantType = AssistantType.System.ONLINE;
            var browser = new BrowserContent(project);
            var content = contentFactory.createContent(browser.getContentPanel(), assistantType.displayName(), false);
            content.putUserData(ACTIVE_TAB, assistantType);
            content.setCloseable(false);

            return new AssistantContent(browser, content);
        }
    }

    record AssistantContent(ChatLinkProvider provider, Content content) {

        public ChatLink getChatLink() {
            return provider.getChatLink();
        }
    }
}
