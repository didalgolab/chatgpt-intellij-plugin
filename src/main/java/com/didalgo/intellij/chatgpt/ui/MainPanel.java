/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import com.didalgo.gpt3.ModelType;
import com.didalgo.intellij.chatgpt.chat.*;
import com.didalgo.intellij.chatgpt.core.ChatCompletionParser;
import com.didalgo.intellij.chatgpt.ui.context.stack.CodeFragmentInfo;
import com.didalgo.intellij.chatgpt.ui.context.stack.ListStack;
import com.didalgo.intellij.chatgpt.ui.context.stack.ListStackFactory;
import com.didalgo.intellij.chatgpt.settings.OpenAISettingsPanel;
import com.didalgo.intellij.chatgpt.settings.OpenAISettingsState;
import com.didalgo.intellij.chatgpt.text.TextFragment;
import com.didalgo.intellij.chatgpt.ui.action.tool.SettingsAction;
import com.didalgo.intellij.chatgpt.ui.listener.SubmitListener;
import com.intellij.icons.AllIcons;
import com.intellij.ide.ui.laf.darcula.ui.DarculaButtonUI;
import com.intellij.notification.BrowseNotificationAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.OnePixelSplitter;
import com.didalgo.intellij.chatgpt.ChatGptBundle;
import com.intellij.util.ui.JBUI;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.reactivex.disposables.Disposable;
import okhttp3.internal.http2.StreamResetException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscription;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

import static java.awt.event.InputEvent.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class MainPanel implements ChatMessageListener {

    private final ExpandableTextFieldExt searchTextField;
    private final JButton button;
    private final JButton stopGenerating;
    private final MessageGroupComponent contentPanel;
    private final JProgressBar progressBar;
    private final OnePixelSplitter splitter;
    private final Project myProject;
    private JPanel actionPanel;
    private volatile Object requestHolder;
    private final MainConversationHandler conversationHandler;
    private ListStack contextStack;
    private final ChatLink chatLink;

    public static final KeyStroke SUBMIT_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, CTRL_DOWN_MASK);

    public MainPanel(@NotNull Project project, ConfigurationPage configuration) {
        myProject = project;
        conversationHandler = new MainConversationHandler(this);
        chatLink = new ChatLinkService(project, conversationHandler, configuration.withSystemPrompt(() -> getContentPanel().getSystemMessage()));
        chatLink.addChatMessageListener(this);
        ContextAwareSnippetizer snippetizer = ApplicationManager.getApplication().getService(ContextAwareSnippetizer.class);
        SubmitListener submitAction = new SubmitListener(chatLink, this::getSearchText, snippetizer);

        splitter = new OnePixelSplitter(true,.98f);
        splitter.setDividerWidth(1);
        splitter.putClientProperty(HyperlinkListener.class, submitAction);

        searchTextField = new ExpandableTextFieldExt(project);
        var searchTextDocument = (AbstractDocument) searchTextField.getDocument();
        searchTextDocument.setDocumentFilter(new NewlineFilter());
        searchTextDocument.putProperty("filterNewlines", Boolean.FALSE);
        searchTextDocument.addDocumentListener(new ExpandableTextFieldExt.ExpandOnMultiLinePaste(searchTextField));
        searchTextField.setMonospaced(false);
        searchTextField.addActionListener(submitAction);
        searchTextField.registerKeyboardAction(submitAction, SUBMIT_KEYSTROKE, JComponent.WHEN_FOCUSED);
        searchTextField.getEmptyText().setText("Type a prompt here");
        button = new JButton(submitAction);
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
        actionPanel.add(createContextSnippetsComponent(), BorderLayout.NORTH);
        actionPanel.add(searchTextField, BorderLayout.CENTER);
        actionPanel.add(button, BorderLayout.EAST);
        contentPanel = new MessageGroupComponent(chatLink, project);
        contentPanel.add(progressBar, BorderLayout.SOUTH);

        splitter.setFirstComponent(contentPanel);
        splitter.setSecondComponent(actionPanel);
    }

    private JComponent createContextSnippetsComponent() {
        // Creating an instance of ListPopupShower for testing
        ListStackFactory listStackFactory = new ListStackFactory();

        // Showing the list popup
        InputContext chatInputContext = getChatLink().getInputContext();
        contextStack = listStackFactory.showListPopup(actionPanel, getProject(), chatInputContext, this::computeTokenCount);
        JList list = contextStack.getList();
        list.setBackground(actionPanel.getBackground());
        list.setBorder(JBUI.Borders.emptyTop(3));
        list.setFocusable(false);
        list.getModel().addListDataListener(new ContextStackHandler());
        list.setVisible(false);
        contextStack.beforeShow();

        chatInputContext.addListener(event -> {
            contextStack.getListModel().syncModel();

            actionPanel.revalidate();
        });

        return list;
    }

    private int computeTokenCount(CodeFragmentInfo info) {
        var tokenCount = 0;
        if (info.getCodeFragment().isPresent())
            tokenCount = getModelType().getTokenizer().encode(info.getCodeFragment().get().toMarkdownString()).size();
        info.setTokenCount(tokenCount);

        SwingUtilities.invokeLater(() -> {
            contextStack.getListModel().syncModel();

            actionPanel.revalidate();
        });
        return tokenCount;
    }

    private class ContextStackHandler implements ListDataListener {

        protected void onContentsChange() {
            var hasContext = !getChatLink().getInputContext().isEmpty();
            if (hasContext != contextStack.getList().isVisible())
                contextStack.getList().setVisible(hasContext);
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            onContentsChange();
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            onContentsChange();
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            onContentsChange();
        }
    }

    public final ChatLink getChatLink() {
        return chatLink;
    }

    public ModelType getModelType() {
        return getChatLink().getConversationContext().getModelType();
    }

    @Override
    public void exchangeStarting(ChatMessageEvent.Starting event) throws ChatExchangeAbortException {
        if (!presetCheck()) {
            throw new ChatExchangeAbortException("Preset check failed");
        }

        TextFragment userMessage = TextFragment.of(event.getUserMessage().getContent());
        question = new MessageComponent(userMessage, null);
        answer = new MessageComponent(TextFragment.of("Thinking..."), getModelType());
        SwingUtilities.invokeLater(() -> {
            setSearchText("");
            aroundRequest(true);

            MessageGroupComponent contentPanel = getContentPanel();
            contentPanel.add(question);
            contentPanel.add(answer);
        });
    }

    private volatile MessageComponent question, answer;

    @Override
    public void exchangeStarted(ChatMessageEvent.Started event) {
        setRequestHolder(event.getSubscription());

        SwingUtilities.invokeLater(contentPanel::updateLayout);
    }

    protected boolean presetCheck() {
        OpenAISettingsState instance = OpenAISettingsState.getInstance();
        String page = getChatLink().getConversationContext().getModelPage();
        if (StringUtils.isEmpty(instance.getConfigurationPage(page).getApiKey())) {
            Notification notification = new Notification(ChatGptBundle.message("group.id"),
                    ChatGptBundle.message("notify.config.title"),
                    ChatGptBundle.message("notify.config.text"),
                    NotificationType.ERROR);
            notification.addAction(new SettingsAction(ChatGptBundle.message("notify.config.action.config"), OpenAISettingsPanel.getTargetPanelClassForPage(page)));
            notification.addAction(new BrowseNotificationAction(ChatGptBundle.message("notify.config.action.browse"), ChatGptBundle.message("notify.config.action.browse.url")));
            Notifications.Bus.notify(notification);
            return false;
        }
        return true;
    }

    @Override
    public void responseArriving(ChatMessageEvent.ResponseArriving event) {
        setContent(event.getPartialResponseChoices());
    }

    @Override
    public void responseArrived(ChatMessageEvent.ResponseArrived event) {
        setContent(event.getResponseChoices());
        SwingUtilities.invokeLater(() -> {
            aroundRequest(false);
        });
    }

    public void setContent(List<ChatMessage> content) {
        TextFragment parseResult = ChatCompletionParser.parseGPT35TurboWithStream(content);
        answer.setContent(parseResult);
    }

    @Override
    public void exchangeFailed(ChatMessageEvent.Failed event) {
        if (answer != null) {
            answer.setErrorContent(getErrorMessage(event.getCause()));
        }
        aroundRequest(false);
    }

    private String getErrorMessage(Throwable cause) {
        if (cause == null)
            return "";
        return (isEmpty(cause.getMessage()) ? "" : cause.getMessage() + "; ")
                + getErrorMessage(cause.getCause());
    }

    @Override
    public void exchangeCancelled(ChatMessageEvent.Cancelled event) {

    }

    public void responseArrivalFailed(ChatMessageEvent.Failed event) {
        if (event.getCause() instanceof StreamResetException) {
            answer.setErrorContent("*Request failure*, cause: " + event.getCause().getMessage());
            aroundRequest(false);
            event.getCause().printStackTrace();
            return;
        }
        answer.setErrorContent("*Response failure*, cause: " + event.getCause().getMessage() + ", please try again.\n\n Tips: if proxy is enabled, please check if the proxy server is working.");
        SwingUtilities.invokeLater(() -> {
            aroundRequest(false);
            contentPanel.scrollToBottom();
        });
    }

    public Project getProject() {
        return myProject;
    }

    public String getSearchText() {
        return searchTextField.getText();
    }

    public void setSearchText(String t) {
        searchTextField.setText(t);
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
            actionPanel.remove(button);
            actionPanel.add(stopGenerating, BorderLayout.EAST);
        } else {
            actionPanel.remove(stopGenerating);
            actionPanel.add(button, BorderLayout.EAST);
        }
        actionPanel.revalidate();
        actionPanel.repaint();
    }

    public void setRequestHolder(Object eventSource) {
        this.requestHolder = eventSource;
    }

}
