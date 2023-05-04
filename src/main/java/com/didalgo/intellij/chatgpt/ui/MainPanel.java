/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import com.didalgo.intellij.chatgpt.chat.*;
import com.didalgo.intellij.chatgpt.core.ChatCompletionParser;
import com.didalgo.intellij.chatgpt.settings.OpenAISettingsState;
import com.didalgo.intellij.chatgpt.ui.listener.SubmitListener;
import com.intellij.icons.AllIcons;
import com.intellij.ide.ui.laf.darcula.ui.DarculaButtonUI;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.didalgo.intellij.chatgpt.ChatGptBundle;
import io.reactivex.disposables.Disposable;
import okhttp3.Call;
import okhttp3.internal.http2.StreamResetException;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscription;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.KeyEvent;

import static java.awt.event.InputEvent.*;

public class MainPanel implements ChatMessageListener {

    private final ExpandableTextField searchTextArea;
    private final JButton button;
    private final JButton stopGenerating;
    private final MessageGroupComponent contentPanel;
    private final JProgressBar progressBar;
    private final OnePixelSplitter splitter;
    private final Project myProject;
    private JPanel actionPanel;
    private Object requestHolder;
    private final MainConversationHandler conversationHandler;
    private final ChatLink chatLink;

    public static final KeyStroke SUBMIT_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, CTRL_DOWN_MASK);

    public MainPanel(@NotNull Project project, ChatLinkStateConfiguration configuration) {
        myProject = project;
        conversationHandler = new MainConversationHandler(this);
        chatLink = new ChatLinkService(project, conversationHandler, configuration.withSystemPrompt(() -> getContentPanel().getSystemMessage()));
        chatLink.addChatMessageListener(this);
        ContextAwareSnippetizer snippetizer = ApplicationManager.getApplication().getService(ContextAwareSnippetizer.class);
        SubmitListener listener = new SubmitListener(chatLink, this::getSearchText, snippetizer);

        splitter = new OnePixelSplitter(true,.98f);
        splitter.setDividerWidth(1);

        searchTextArea = new ExpandableTextField(
                text -> StringUtil.split(text, NewlineFilter.NEWLINE_REPLACEMENT.toString()),
                lines -> String.join(NewlineFilter.NEWLINE_REPLACEMENT.toString(), lines)) {

            @Override
            public String getText() {
                return NewlineFilter.normalize(super.getText());
            }

            @Override
            public String getText(int offs, int len) throws BadLocationException {
                return NewlineFilter.normalize(super.getText(offs, len));
            }

            @Override
            public String getSelectedText() {
                return NewlineFilter.normalize(super.getSelectedText());
            }
        };
        var searchTextDocument = (AbstractDocument) searchTextArea.getDocument();
        searchTextDocument.setDocumentFilter(new NewlineFilter());
        searchTextDocument.putProperty("filterNewlines", Boolean.FALSE);
        searchTextArea.setMonospaced(false);
        searchTextArea.addActionListener(listener);
        searchTextArea.registerKeyboardAction(listener, SUBMIT_KEYSTROKE, JComponent.WHEN_FOCUSED);
        button = new JButton(ChatGptBundle.message("ui.toolwindow.send"), IconLoader.getIcon("/icons/send.svg", MainPanel.class));
        button.addActionListener(listener);
        button.setUI(new DarculaButtonUI());

        stopGenerating = new JButton("Stop", AllIcons.Actions.Suspend);
        stopGenerating.addActionListener(e -> {
            aroundRequest(false);
            if (requestHolder instanceof Disposable disposable) {
                disposable.dispose();
            } else if (requestHolder instanceof Subscription subscription) {
                subscription.cancel();
            }
        });
        stopGenerating.setUI(new DarculaButtonUI());

        actionPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        actionPanel.add(searchTextArea, BorderLayout.CENTER);
        actionPanel.add(button, BorderLayout.EAST);
        contentPanel = new MessageGroupComponent(chatLink, project);
        contentPanel.add(progressBar, BorderLayout.SOUTH);

        splitter.setFirstComponent(contentPanel);
        splitter.setSecondComponent(actionPanel);
    }

    public final ChatLink getChatLink() {
        return chatLink;
    }

    @Override
    public void exchangeStarting(ChatMessageEvent.Starting event) throws ChatExchangeAbortException {
        if (!presetCheck()) {
            throw new ChatExchangeAbortException("Preset check failed");
        }

        // Reset the question container
        setSearchText("");
        aroundRequest(true);

        MessageGroupComponent contentPanel = getContentPanel();

        // TBR: If required, attach editor's selected code
        String userMessage = event.getUserMessage().getContent();

        // Add the message component to container
        question = new MessageComponent(escapeHtml(userMessage),true);
        answer = new MessageComponent("Thinking...",false);
        contentPanel.add(question);
        contentPanel.add(answer);

    }

    private volatile MessageComponent question, answer;

    @Override
    public void exchangeStarted(ChatMessageEvent.Started event) {
        setRequestHolder(event.getSubscription());
        contentPanel.updateLayout();
        contentPanel.scrollToBottom();
    }

    protected boolean presetCheck() {
        OpenAISettingsState instance = OpenAISettingsState.getInstance();
        if (com.didalgo.intellij.chatgpt.util.StringUtil.isEmpty(instance.getConfigForCategory(getChatLink().getConversationContext().getGroup()).getApiKey())) {
            Notifications.Bus.notify(
                    new Notification(ChatGptBundle.message("group.id"),
                            "Wrong setting",
                            "Please configure an API Key first.",
                            NotificationType.ERROR));
            return false;
        }
        return true;
    }

    protected String escapeHtml(String text) {
        return text.replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "\n<br>");
    }

    @Override
    public void responseArriving(ChatMessageEvent.ResponseArriving event) {
        try {
            ChatCompletionParser.ParseResult parseResult = ChatCompletionParser.
                    parseGPT35TurboWithStream(event.getPartialResponseChoices());

            // Copy action only needed source content
            answer.setSourceContent(parseResult.getSource());
            answer.setContent(parseResult.getHtml());
        } catch (Exception e) {
            answer.setContent(e.getMessage());
        }
    }

    @Override
    public void responseArrived(ChatMessageEvent.ResponseArrived event) {
        aroundRequest(false);
        contentPanel.scrollToBottom();
    }

    @Override
    public void exchangeFailed(ChatMessageEvent.Failed event) {
        if (answer != null) {
            answer.setSourceContent(event.getCause().getMessage());
            answer.setContent(event.getCause().getMessage());
        }
        aroundRequest(false);
    }

    @Override
    public void exchangeCancelled(ChatMessageEvent.Cancelled event) {

    }

    public void responseArrivalFailed(ChatMessageEvent.Failed event) {
        if (event.getCause() instanceof StreamResetException) {
            answer.setContent("Request failure, cause: " + event.getCause().getMessage());
            aroundRequest(false);
            event.getCause().printStackTrace();
            return;
        }
        answer.setContent("Response failure, cause: " + event.getCause().getMessage() + ", please try again. <br><br> Tips: if proxy is enabled, please check if the proxy server is working.");
        aroundRequest(false);
        contentPanel.scrollToBottom();
    }

    private static class NewlineFilter extends DocumentFilter {
        private static final Character NEWLINE_REPLACEMENT = '\u23CE';

        @Override
        public void insertString(FilterBypass fb, int offset, String text,
                                 AttributeSet attr) throws BadLocationException {
            text = denormalize(text);
            super.insertString(fb, offset, text, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text,
                            AttributeSet attrs) throws BadLocationException {
            text = denormalize(text);
            super.replace(fb, offset, length, text, attrs);
        }

        public static String denormalize(String text) {
            if (text == null || text.isEmpty()) {
                return text;
            }
            return text.replace('\n', NEWLINE_REPLACEMENT).replace("\r", "");
        }

        public static String normalize(String text) {
            if (text == null || text.isEmpty()) {
                return text;
            }
            return text.replace(NEWLINE_REPLACEMENT, '\n');
        }
    }

    public Project getProject() {
        return myProject;
    }

    public String getSearchText() {
        return searchTextArea.getText();
    }

    public void setSearchText(String t) {
        searchTextArea.setText(t);
    }

    public MessageGroupComponent getContentPanel() {
        return contentPanel;
    }

    public JPanel init() {
        return splitter;
    }

    public void aroundRequest(boolean status) {
        progressBar.setIndeterminate(status);
        progressBar.setVisible(status);
        button.setEnabled(!status);
        if (status) {
            contentPanel.addScrollListener();
            actionPanel.remove(button);
            actionPanel.add(stopGenerating,BorderLayout.EAST);
        } else {
            contentPanel.removeScrollListener();
            actionPanel.remove(stopGenerating);
            actionPanel.add(button,BorderLayout.EAST);
        }
        actionPanel.revalidate();
        actionPanel.repaint();
    }

    public void setRequestHolder(Object eventSource) {
        this.requestHolder = eventSource;
    }

}
