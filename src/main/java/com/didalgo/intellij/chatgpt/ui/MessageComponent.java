/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import com.didalgo.intellij.chatgpt.ChatGptBundle;
import com.didalgo.intellij.chatgpt.text.TextFragment;
import com.didalgo.intellij.chatgpt.util.ImgUtils;
import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ui.*;
import com.didalgo.intellij.chatgpt.ChatGptIcons;
import com.didalgo.intellij.chatgpt.settings.OpenAISettingsState;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicReference;

public class MessageComponent extends JBPanel<MessageComponent> {

    private static final Logger LOG = LoggerFactory.getLogger(MessageComponent.class);

    private final MessagePanel component = new MessagePanel();

    private TextFragment text;

    public MessageComponent(TextFragment text, boolean me) {
        this.text = text;
        setDoubleBuffered(true);
        setOpaque(true);
        setBackground(me ? new JBColor(0xEAEEF7, 0x45494A) : new JBColor(0xE0EEF7, 0x2d2f30 /*2d2f30*/));
        setBackground(me ? new JBColor(0xF7F7F7, 0x3C3F41) : new JBColor(0xEBEBEB, 0x4C4E50 /*2d2f30*/));
        setBorder(JBUI.Borders.empty(10, 10, 10, 0));
        setLayout(new BorderLayout(JBUI.scale(2), 0));

        if (OpenAISettingsState.getInstance().isEnableAvatar()) {
            JPanel iconPanel = new JPanel(new BorderLayout());
            iconPanel.setOpaque(false);
            Image imageIcon;
            try {
                imageIcon = me ? ImgUtils.iconToImage(ChatGptIcons.ME) : ImgUtils.iconToImage(ChatGptIcons.OPEN_AI);
            } catch (Exception e) {
                imageIcon = me ? ImgUtils.iconToImage(ChatGptIcons.ME) : ImgUtils.iconToImage(ChatGptIcons.AI);
            }
            Image scale = ImageUtil.scaleImage(imageIcon, 30, 30);
            iconPanel.add(new JBLabel(new ImageIcon(scale)), BorderLayout.NORTH);
            add(iconPanel, BorderLayout.WEST);
        }
        JPanel centerPanel = new JPanel(new VerticalLayout(JBUI.scale(8)));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(JBUI.Borders.emptyLeft(JBUI.scale(5)));
        centerPanel.add(createContentComponent(text));
        add(centerPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);
        actionPanel.setBorder(JBUI.Borders.emptyRight(JBUI.scale(12)));
        JLabel copyAction = new JLabel(AllIcons.Actions.Copy);
        copyAction.setCursor(new Cursor(Cursor.HAND_CURSOR));
        copyAction.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Transferable transferable = new StringSelection(getText().markdown());
                CopyPasteManager.getInstance().setContents(transferable);
                Notifications.Bus.notify(
                        new Notification(ChatGptBundle.message("group.id"),
                                "Copied successfully",
                                "ChatGPT " + (me? "prompt":"reply") + " content has been successfully copied to the clipboard.",
                                NotificationType.INFORMATION));
            }
        });
        actionPanel.add(copyAction, BorderLayout.NORTH);
        add(actionPanel, BorderLayout.EAST);
    }

    public TextFragment getText() {
        return text;
    }

    public Component createContentComponent(TextFragment content) {

        component.setEditable(false);
        component.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, java.lang.Boolean.TRUE);
        component.setContentType("text/html; charset=UTF-8");
        component.setOpaque(false);
        component.setBorder(null);

        configureHtmlEditorKit2(component, false);
        component.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY, getText().markdown());

        component.updateMessage(content);

        component.setEditable(false);
        if (component.getCaret() != null) {
            component.setCaretPosition(0);
        }

        component.revalidate();
        component.repaint();

        return component;
    }

    public void configureHtmlEditorKit2(@NotNull JEditorPane editorPane, boolean notificationColor) {
        HTMLEditorKit kit = new HTMLEditorKitBuilder()
                .withViewFactoryExtensions((e, v) -> component.createView(e, v), ExtendableHTMLViewFactory.Extensions.WORD_WRAP)
                .withFontResolver(new CSSFontResolver() {
            @Override
            public @NotNull Font getFont(@NotNull Font defaultFont, @NotNull AttributeSet attributeSet) {
                if ("a".equalsIgnoreCase(String.valueOf(attributeSet.getAttribute(AttributeSet.NameAttribute)))) {
                    return UIUtil.getLabelFont();
                }
                return defaultFont;
            }
        }).build();
        String color = ColorUtil.toHtmlColor(notificationColor ? getLinkButtonForeground() : JBUI.CurrentTheme.Link.Foreground.ENABLED);
        kit.getStyleSheet().addRule("a {color: " + color + "}");
        editorPane.setEditorKit(kit);
    }

    public static @NotNull Color getLinkButtonForeground() {
        return JBColor.namedColor("Notification.linkForeground", JBUI.CurrentTheme.Link.Foreground.ENABLED);
    }

    private final AtomicReference<TextFragment> content = new AtomicReference<>();
    private final Timer updateContentTimer = new Timer(20, this::updateIncrementalContent);

    public void setContent(TextFragment content) {
        this.text = content;
        this.content.set(content);
        if (!updateContentTimer.isRunning()) {
            updateContentTimer.setRepeats(false);
            updateContentTimer.start();
        }
    }

    public void setErrorContent(String errorMessage) {
        setContent(TextFragment.of(errorMessage));
        component.setForeground(JBColor.RED);
    }

    protected void updateIncrementalContent(ActionEvent event) {
        TextFragment message = null;
        try {
            message = content.get();
            if (message != null) {
                component.updateMessage(message);
                content.compareAndSet(message, null);
            }
        } catch (Exception e) {
            LOG.error("ChatGPT Exception in processing response: response: {}, error: {}", message, e.getMessage());
            e.printStackTrace();
        }
    }

    public void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            Rectangle bounds = getBounds();
            scrollRectToVisible(bounds);
        });
    }
}
