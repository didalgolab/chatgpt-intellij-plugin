/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import com.didalgo.intellij.chatgpt.chat.*;
import com.didalgo.intellij.chatgpt.ui.action.browser.*;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import reactor.core.Disposable;
import reactor.core.Disposables;

import java.awt.*;
import javax.swing.*;

/**
 * The BrowserContent class provides a panel that displays a JCEF Browser with ChatGPT interface.
 * It also contains methods to execute user queries on the chatbot.
 * <p>
 * The panel includes a toolbar with options to refresh the page, clear cookies, and adjust zoom level.
 * If JCEF is not supported by the current IDE, a message is displayed instead of the browser.
 */
public class BrowserContent implements ChatLinkProvider {

    public static final String DEFAULT_URL = "https://chat.openai.com/chat";
    private final JPanel contentPanel;
    private final JBCefBrowser browser;
    private final ChatLink chatLink;

    public BrowserContent(Project project) {
        this(project, DEFAULT_URL);
    }

    public BrowserContent(Project project, String url) {
        contentPanel = new JPanel(new BorderLayout());
        browser = new JBCefBrowser(url);
        chatLink = new ChatLinkService(project, new BrowserConversationHandler(), null);

        if (!JBCefApp.isSupported()) {
            contentPanel.add(theJCEFisNotStrongWithThisOne(), BorderLayout.CENTER);
            return;
        }

        JComponent component = browser.getComponent();
        DefaultActionGroup toolbarActions = new DefaultActionGroup();
        toolbarActions.add(new RefreshPageAction(browser));
        toolbarActions.add(new Separator());
        toolbarActions.add(new ClearCookiesAction(browser, contentPanel));
        toolbarActions.add(new Separator());
        toolbarActions.add(new ZoomInAction(browser));
        toolbarActions.add(new ZoomOutAction(browser));
        toolbarActions.add(new ZoomResetAction(browser));
        ActionToolbarImpl browserToolbar = new ActionToolbarImpl("Browser Toolbar", toolbarActions, true);
        browserToolbar.setTargetComponent(null);
        contentPanel.add(browserToolbar, BorderLayout.NORTH);
        contentPanel.add(component, BorderLayout.CENTER);
    }

    @Override
    public ChatLink getChatLink() {
        return chatLink;
    }

    @NotNull
    private static JTextPane theJCEFisNotStrongWithThisOne() {
        String message = "The current IDE does not support Online ChatGPT, because the JVM runtime does not support JCEF.";
        JTextPane area = new JTextPane();
        area.setEditable(false);
        area.setText(message);
        area.setBorder(JBUI.Borders.empty(10));
        return area;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void handleUserInput(String text) {
        text = text.replace("'", "\\'");

        String fillQuestion = "document.getElementsByTagName(\"textarea\")[0].value = '" + text + "'";
        String enableButton = "document.getElementsByTagName(\"textarea\")[0].nextSibling.removeAttribute('disabled')";
        String doClick = "document.getElementsByTagName(\"textarea\")[0].nextSibling.click()";
        // Fill the text
        String formattedQuestion = fillQuestion.replace("\n", "\\n");
        browser.getCefBrowser().executeJavaScript(formattedQuestion, DEFAULT_URL, 0);
        browser.getCefBrowser().executeJavaScript(enableButton, DEFAULT_URL, 0);
        browser.getCefBrowser().executeJavaScript(doClick, DEFAULT_URL, 0);
    }

    private class BrowserConversationHandler implements ConversationHandler {

        @Override
        public Disposable push(ConversationContext ctx, ChatMessageEvent.Starting event, ChatMessageListener listener) {
            handleUserInput(event.getUserMessage().getContent());
            return Disposables.never();
        }
    }
}

