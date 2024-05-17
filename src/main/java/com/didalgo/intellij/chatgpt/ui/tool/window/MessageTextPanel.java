/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.tool.window;

import com.didalgo.intellij.chatgpt.text.TextFragment;
import com.didalgo.intellij.chatgpt.ui.MessageRenderer;
import com.didalgo.intellij.chatgpt.ui.view.*;
import com.didalgo.intellij.chatgpt.util.StandardLanguage;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.ExtendableHTMLViewFactory;
import com.intellij.util.ui.HTMLEditorKitBuilder;
import com.intellij.util.ui.HtmlPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;

public class MessageTextPanel extends HtmlPanel implements MessageRenderer {

    private volatile TextFragment text;

    public MessageTextPanel() {
        setEditorKit(new HTMLEditorKitBuilder()
                .withViewFactoryExtensions(this::createView, ExtendableHTMLViewFactory.Extensions.WORD_WRAP)
                .build());
        setOpaque(true);
    }

    protected View createView(Element elem, View view) {
        AttributeSet attrs = elem.getAttributes();
        if (attrs.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.DIV && supportsCollapsibility(attrs))
            return CollapsiblePanelFactory.createPanel(this, elem, attrs);
        if (attrs.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.PRE)
            return new RSyntaxTextAreaView(elem, LanguageDetector.getLanguage(elem).orElse(StandardLanguage.NONE));

        return view;
    }

    protected boolean supportsCollapsibility(AttributeSet attrs) {
        return CollapsiblePanelFactory.supportsCollapsibility(this, attrs);
    }

    @Override
    protected @NotNull @Nls String getBody() {
        return (text == null)? "" : text.toHtml();
    }

    @Override
    protected @NotNull Font getBodyFont() {
        return UIUtil.getLabelFont();
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
            HyperlinkHandler.handleOrElse(e, super::hyperlinkUpdate);
    }

    public void updateMessage(TextFragment updateMessage) {
        this.text = updateMessage;
        update();
    }

    public HTMLEditorKit configureHtmlEditorKit2(@NotNull JEditorPane editorPane, boolean notificationColor) {
        HTMLEditorKit kit = new HTMLEditorKitBuilder()
                .withViewFactoryExtensions((e, v) -> createView(e, v), ExtendableHTMLViewFactory.Extensions.WORD_WRAP)
                .withFontResolver((defaultFont, attributeSet) -> {
                    if ("a".equalsIgnoreCase(String.valueOf(attributeSet.getAttribute(AttributeSet.NameAttribute))))
                        return UIUtil.getLabelFont();
                    else
                        return defaultFont;
                }).build();
        String color = ColorUtil.toHtmlColor(notificationColor ? getLinkButtonForeground() : JBUI.CurrentTheme.Link.Foreground.ENABLED);
        kit.getStyleSheet().addRule("a {color: " + color + "}");
        kit.getStyleSheet().addRule("p {margin:4px 0}");
        editorPane.setEditorKit(kit);
        return kit;
    }

    public static @NotNull Color getLinkButtonForeground() {
        return JBColor.namedColor("Notification.linkForeground", JBUI.CurrentTheme.Link.Foreground.ENABLED);
    }
}
