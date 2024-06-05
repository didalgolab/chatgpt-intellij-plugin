/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.tool.window;

import com.didalgo.intellij.chatgpt.SystemMessageHolder;
import com.didalgo.intellij.chatgpt.chat.ChatLink;
import com.didalgo.intellij.chatgpt.chat.metadata.ImmutableUsage;
import com.didalgo.intellij.chatgpt.event.ListenerList;
import com.didalgo.intellij.chatgpt.event.ListenerList.Subscription;
import com.didalgo.intellij.chatgpt.ui.text.ExpandableTextFieldExt;
import com.didalgo.intellij.chatgpt.util.ScrollingTools;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.NullableComponent;
import com.intellij.ui.Gray;
import com.intellij.ui.HideableTitledPanel;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.didalgo.intellij.chatgpt.settings.GeneralSettings;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static com.didalgo.intellij.chatgpt.settings.GeneralSettings.BASE_PROMPT;

public class ConversationPanel extends JBPanel<ConversationPanel> implements NullableComponent, SystemMessageHolder {
    private final JPanel myList = new JPanel(new VerticalLayout(0));
    private final JBScrollPane myScrollPane = new JBScrollPane(myList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    private int myScrollValue = 0;
    private UsagePanel usagePanel;
    private JBTextField systemRole;
    private final Project project;
    private final ChatLink chatLink;
    private final ListenerList<Runnable> onChatMemoryCleared = ListenerList.of(Runnable.class);

    public ConversationPanel(ChatLink chatLink, @NotNull Project project) {
        this.chatLink = chatLink;
        this.project = project;
        setBorder(JBUI.Borders.empty());
        setLayout(new BorderLayout());
        setBackground(UIUtil.getListBackground());

        myScrollPane.getVerticalScrollBar().putClientProperty(JBScrollPane.IGNORE_SCROLLBAR_IN_INSETS, Boolean.TRUE);
        ScrollingTools.installAutoScrollToBottom(myScrollPane);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(JBUI.Borders.emptyLeft(0));

        if (true) {
            JPanel panel = new NonOpaquePanel(new GridLayout(0,1));
            JPanel rolePanel = new NonOpaquePanel(new BorderLayout());
            systemRole = new ExpandableTextFieldExt(project, null);
            GeneralSettings instance = GeneralSettings.getInstance();
            systemRole.setText(instance.gpt35RoleText);
            rolePanel.add(systemRole, BorderLayout.CENTER);
            DefaultActionGroup toolbarActions = new DefaultActionGroup();
            toolbarActions.add(new AnAction(AllIcons.Actions.MenuSaveall) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    instance.gpt35RoleText = systemRole.getText().isEmpty() ? BASE_PROMPT : systemRole.getText();
                }
            });
            toolbarActions.add(new AnAction(AllIcons.Actions.Rollback) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    systemRole.setText(BASE_PROMPT);
                    instance.setGpt35RoleText(BASE_PROMPT);
                }
            });
            ActionToolbarImpl actonPanel = new ActionToolbarImpl("System Role Toolbar",toolbarActions,true);
            actonPanel.setTargetComponent(this);
            rolePanel.add(actonPanel,BorderLayout.EAST);
            panel.add(rolePanel);
            panel.setBorder(JBUI.Borders.empty(0,8,10,0));

            HideableTitledPanel cPanel = new HideableTitledPanel("System role: you can guide your assistant and define its behavior.", false);
            cPanel.setContentComponent(panel);
            cPanel.setOn(false);
            cPanel.setBorder(JBUI.Borders.empty(0,8,10,0));
            add(cPanel, BorderLayout.NORTH);
        }

        add(mainPanel, BorderLayout.CENTER);

        JBLabel myTitle = new JBLabel("Conversation");
        myTitle.setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(Gray.x80, Gray.x8C)));
        myTitle.setFont(JBFont.label());

        this.usagePanel = new UsagePanel();

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(0,10,10,0));

        //panel.add(myTitle, BorderLayout.WEST);

        LinkLabel<String> newChat = new LinkLabel<>("New chat", null);
        newChat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                myList.removeAll();
                addAssistantTipsIfEnabled(false);
                myList.updateUI();
                chatLink.getConversationContext().clear();
                onChatMemoryCleared.fire().run();
                usagePanel.updateUsage(ImmutableUsage.empty());
            }
        });

        newChat.setFont(JBFont.label());
        newChat.setBorder(JBUI.Borders.emptyRight(20));
        panel.add(newChat, BorderLayout.EAST);
        panel.add(usagePanel, BorderLayout.WEST);
        mainPanel.add(panel, BorderLayout.NORTH);

        myList.setOpaque(true);
        myList.setBackground(UIUtil.getListBackground());
        myList.setBorder(JBUI.Borders.emptyRight(0));

        myScrollPane.setBorder(JBUI.Borders.empty());
        mainPanel.add(myScrollPane);
        myScrollPane.getVerticalScrollBar().setAutoscrolls(true);
        myScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            myScrollValue = e.getValue();
        });

        addAssistantTipsIfEnabled(true);
    }

    public void addSeparator(JComponent comp) {
        SwingUtilities.invokeLater(() -> {
            JSeparator separator = new JSeparator();
            separator.setForeground(JBColor.border());
            comp.add(separator);
            updateLayout();
            invalidate();
            validate();
            repaint();
        });
    }

    protected void addAssistantTipsIfEnabled(boolean firstUse) {
        addSeparator(myList);

        var introEnabled = GeneralSettings.getInstance().getEnableInitialMessage();
        if (!firstUse && introEnabled == null)
            GeneralSettings.getInstance().setEnableInitialMessage(introEnabled = false);
        if (!Boolean.FALSE.equals(introEnabled))
            myList.add(createAssistantTips());
    }

    protected ConversationTurnPanel createAssistantTips() {
        var modelType = chatLink.getConversationContext().getModelType();
        return new ConversationTurnPanel(new AssistantMessage("""
                Hi, I'm your AI-powered annoying pair programmer. How can I assist you today?
                
                Here are some suggestions to get you started:
                [✦ Explain the selected code](assistant://?prompt=Explain+the+selected+code)
                [✦ Convert this Oracle SQL to PostgreSQL](assistant://?prompt=Convert+this+Oracle+SQL+to+PostgreSQL)
                [✦ What for can I use atomics in Java?](assistant://?prompt=What+for+can+I+use+atomics+in+Java%3F)
                [✦ Explain the LazyHolder pattern in Java](assistant://?prompt=Explain+the+LazyHolder+pattern+in+Java)
                [✦ Suggest Java library's method for doing OCR](assistant://?prompt=Suggest+Java+library%27s+method+for+doing+OCR)
                """), modelType);
    }

    public void add(ConversationTurnPanel conversationTurnPanel) {
        SwingUtilities.invokeLater(() -> {
            myList.add(conversationTurnPanel);
            updateLayout();
            scrollToBottom();
            invalidate();
            validate();
            repaint();
        });
    }

    public Subscription onChatMemoryCleared(Runnable action) {
        return onChatMemoryCleared.addListener(action);
    }

    public ConversationTurnPanel getConversationTurnPanel(int n) {
        if (n >= 0)
            return (ConversationTurnPanel) myList.getComponent(n);
        else
            return (ConversationTurnPanel) myList.getComponent(myList.getComponentCount() + n);
    }

    public void scrollToBottom() {
        ScrollingTools.scrollToBottom(myScrollPane);
    }

    public void updateLayout() {
        LayoutManager layout = myList.getLayout();
        int componentCount = myList.getComponentCount();
        for (int i = 0; i < componentCount; i++) {
            layout.removeLayoutComponent(myList.getComponent(i));
            layout.addLayoutComponent(null, myList.getComponent(i));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (myScrollValue > 0) {
            g.setColor(JBColor.border());
            int y = myScrollPane.getY() - 1;
            g.drawLine(0, y, getWidth(), y);
        }
    }

    @Override
    public boolean isVisible() {
        if (super.isVisible()) {
            int count = myList.getComponentCount();
            for (int i = 0 ; i < count ; i++) {
                if (myList.getComponent(i).isVisible()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isNull() {
        return !isVisible();
    }

    @Override
    public String getSystemMessage() {
        return systemRole.getText();
    }

    public void updateUsage(Usage usage) {
        usagePanel.updateUsage(usage);
    }
}
