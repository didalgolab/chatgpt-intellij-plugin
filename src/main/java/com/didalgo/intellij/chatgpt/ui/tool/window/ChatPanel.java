/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.tool.window;

import com.didalgo.intellij.chatgpt.Errors;
import com.didalgo.intellij.chatgpt.chat.*;
import com.didalgo.intellij.chatgpt.chat.models.ModelFamily;
import com.didalgo.intellij.chatgpt.chat.models.ModelType;
import com.didalgo.intellij.chatgpt.core.ChatCompletionParser;
import com.didalgo.intellij.chatgpt.text.TextContent;
import com.didalgo.intellij.chatgpt.ui.ContextAwareSnippetizer;
import com.didalgo.intellij.chatgpt.ui.text.ExpandableTextFieldExt;
import com.didalgo.intellij.chatgpt.ui.InputContextPromptAttachmentHandler;
import com.didalgo.intellij.chatgpt.ui.MainConversationHandler;
import com.didalgo.intellij.chatgpt.ui.text.NewlineFilter;
import com.didalgo.intellij.chatgpt.ui.prompt.context.AbstractPromptAttachment;
import com.didalgo.intellij.chatgpt.ui.prompt.context.ListStack;
import com.didalgo.intellij.chatgpt.ui.prompt.context.ListStackFactory;
import com.didalgo.intellij.chatgpt.settings.GeneralSettings;
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
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscription;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Disposable;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static java.awt.event.InputEvent.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ChatPanel implements ChatMessageListener, ChatLinkProvider {

    private final ExpandableTextFieldExt userMessageTextField;
    private final JButton submitButton;
    private final JButton stopGenerating;
    private final @Getter ConversationPanel contentPanel;
    private final JProgressBar progressBar;
    private final OnePixelSplitter splitter;
    private final Project myProject;
    private JPanel actionPanel;
    private volatile Object requestHolder;
    private final MainConversationHandler conversationHandler;
    private ListStack contextStack;
    private final ChatLink chatLink;

    public static final KeyStroke SUBMIT_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, CTRL_DOWN_MASK);

    public ChatPanel(@NotNull Project project, AssistantConfiguration configuration) {
        myProject = project;
        conversationHandler = new MainConversationHandler(this);
        chatLink = new ChatLinkService(project, conversationHandler, configuration.withSystemPrompt(() -> getContentPanel().getSystemMessage()));
        chatLink.addChatMessageListener(this);
        ContextAwareSnippetizer snippetizer = ApplicationManager.getApplication().getService(ContextAwareSnippetizer.class);
        SubmitListener submitAction = new SubmitListener(chatLink, this::getSearchText, snippetizer);

        splitter = new OnePixelSplitter(true,.98f);
        splitter.setDividerWidth(1);
        splitter.putClientProperty(HyperlinkListener.class, submitAction);

        userMessageTextField = new ExpandableTextFieldExt(project, new InputContextPromptAttachmentHandler(chatLink.getInputContext()));
        var searchTextDocument = (AbstractDocument) userMessageTextField.getDocument();
        searchTextDocument.setDocumentFilter(new NewlineFilter());
        searchTextDocument.putProperty("filterNewlines", Boolean.FALSE);
        searchTextDocument.addDocumentListener(new ExpandableTextFieldExt.ExpandOnMultiLinePaste(userMessageTextField));
        userMessageTextField.setMonospaced(false);
        userMessageTextField.addActionListener(submitAction);
        userMessageTextField.registerKeyboardAction(submitAction, SUBMIT_KEYSTROKE, JComponent.WHEN_FOCUSED);
        userMessageTextField.getEmptyText().setText("Type a prompt here");
        submitButton = new JButton(submitAction);
        submitButton.setUI(new DarculaButtonUI());

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
        actionPanel.add(userMessageTextField, BorderLayout.CENTER);
        actionPanel.add(submitButton, BorderLayout.EAST);
        contentPanel = new ConversationPanel(chatLink, project);
        contentPanel.add(progressBar, BorderLayout.SOUTH);
        contentPanel.onChatMemoryCleared(userMessageTextField::requestFocusInWindow);

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
            userMessageTextField.requestFocusInWindow();
            actionPanel.revalidate();
        });

        return list;
    }

    private int computeTokenCount(PromptAttachment info) {
        var tokenCount = 0;
        if (info.getTextContentIfPresent().isPresent())
            tokenCount = getModelType().getTokenizer().encode(TextContent.toString(info.getTextContentIfPresent().get())).size();
        if (info instanceof AbstractPromptAttachment aInfo)
            aInfo.setTokenCount(tokenCount);

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

    @Override
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

        ApplicationManager.getApplication().invokeAndWait(() -> {
            answer = new ConversationTurnPanel(new AssistantMessage("Thinking..."), getModelType());
        });
        SwingUtilities.invokeLater(() -> {
            setSearchText("");
            aroundRequest(true);

            ConversationPanel contentPanel = getContentPanel();
            contentPanel.add(new ConversationTurnPanel(event.getUserMessage(), null));
            contentPanel.add(answer);
        });
    }

    private volatile ConversationTurnPanel answer;

    @Override
    public void exchangeStarted(ChatMessageEvent.Started event) {
        setRequestHolder(event.getSubscription());

        SwingUtilities.invokeLater(contentPanel::updateLayout);
    }

    protected boolean presetCheck() {
        var settings = GeneralSettings.getInstance();
        var assistantType = getChatLink().getConversationContext().getAssistantType();
        var options = settings.getAssistantOptions(assistantType);
        if (assistantType.getFamily().isApiKeyOptional()) {
            return true;
        }
        if (assistantType.getFamily() == ModelFamily.AZURE_OPENAI) {
            return presetCheckForAzure(assistantType, options);
        }

        if (isEmpty(options.getApiKey())) {
            Notification notification = new Notification(ChatGptBundle.message("group.id"),
                    ChatGptBundle.message("notify.config.title"),
                    ChatGptBundle.message("notify.config.apikey.text"),
                    NotificationType.ERROR);
            notification.addAction(new SettingsAction(ChatGptBundle.message("notify.config.action.config"), assistantType.getConfigurable()));
            notification.addAction(new BrowseNotificationAction(ChatGptBundle.message("notify.config.action.browse"), assistantType.getFamily().getApiKeysHomepage()));
            Notifications.Bus.notify(notification);
            return false;
        }
        return true;
    }

    protected boolean presetCheckForAzure(AssistantType assistantType, GeneralSettings.AssistantOptions options) {
        var apiKey = options.getApiKey();
        var apiEndpoint = options.getAzureApiEndpoint();
        var deploymentName = options.getAzureDeploymentName();

        if (isEmpty(apiKey) || isEmpty(apiEndpoint) || isEmpty(deploymentName)) {
            var missingOpts = new ArrayList<String>();
            if (isEmpty(apiKey)) missingOpts.add("\"API Key\"");
            if (isEmpty(apiEndpoint)) missingOpts.add("\"API Endpoint\"");
            if (isEmpty(deploymentName)) missingOpts.add("\"Deployment Name\"");

            Notification notification = new Notification(ChatGptBundle.message("group.id"),
                    ChatGptBundle.message("notify.config.title"),
                    ChatGptBundle.message("notify.config.opts.text", String.join(", ", missingOpts)),
                    NotificationType.ERROR);
            notification.addAction(new SettingsAction(ChatGptBundle.message("notify.config.action.config"), assistantType.getConfigurable()));
            notification.addAction(new BrowseNotificationAction(ChatGptBundle.message("notify.config.action.browse"), assistantType.getFamily().getApiKeysHomepage()));
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

    public void setContent(List<Generation> content) {
        if (!content.isEmpty()) {
            var generation = content.get(0);
            answer.setContent(generation.getOutput(), ChatCompletionParser.parseTextContent(generation));
        }
    }

    @Override
    public void exchangeFailed(ChatMessageEvent.Failed event) {
        if (answer != null) {
            answer.setErrorContent(Errors.getWebClientErrorMessage(event.getCause()));
        }
        aroundRequest(false);
    }

    @Override
    public void exchangeCancelled(ChatMessageEvent.Cancelled event) {

    }

    public Project getProject() {
        return myProject;
    }

    public String getSearchText() {
        return userMessageTextField.getText();
    }

    public void setSearchText(String t) {
        userMessageTextField.setText(t);
    }

    public ConversationTurnPanel getConversationTurnPanel(int n) {
        return contentPanel.getConversationTurnPanel(n);
    }

    public JPanel init() {
        return splitter;
    }

    public void aroundRequest(boolean status) {
        progressBar.setIndeterminate(status);
        progressBar.setVisible(status);
        submitButton.setEnabled(!status);
        if (status) {
            actionPanel.remove(submitButton);
            actionPanel.add(stopGenerating, BorderLayout.EAST);
        } else {
            actionPanel.remove(stopGenerating);
            actionPanel.add(submitButton, BorderLayout.EAST);
        }
        actionPanel.revalidate();
        actionPanel.repaint();
    }

    public void setRequestHolder(Object eventSource) {
        this.requestHolder = eventSource;
    }

    public JButton getSubmitButton() {
        return submitButton;
    }
}
