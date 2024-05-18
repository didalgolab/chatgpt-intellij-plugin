/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.tool.window;

import com.didalgo.intellij.chatgpt.compat.LegacyHtmlPanel;
import com.didalgo.intellij.chatgpt.text.TextFragment;
import com.didalgo.intellij.chatgpt.ui.MessageRenderer;
import com.didalgo.intellij.chatgpt.ui.view.*;
import com.didalgo.intellij.chatgpt.util.StandardLanguage;
import com.intellij.ui.ColorUtil;
import com.intellij.util.ui.ExtendableHTMLViewFactory;
import com.intellij.util.ui.HTMLEditorKitBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;

public class MessageTextPanel extends LegacyHtmlPanel implements MessageRenderer {

    private final boolean fromUser;
    private volatile TextFragment text;

    public MessageTextPanel(boolean fromUser) {
        setEditorKit(new HTMLEditorKitBuilder()
                .withViewFactoryExtensions(this::createView, ExtendableHTMLViewFactory.Extensions.WORD_WRAP)
                .build());
        setOpaque(true);
        this.fromUser = fromUser;

        HTMLEditorKit kit = (HTMLEditorKit) getEditorKit();
        kit.getStyleSheet().addRule("a {color: " + ColorUtil.toHtmlColor(linkColor()) + "}");
        kit.getStyleSheet().addRule("p {margin:4px 0}");
        if (fromUser) {
            kit.getStyleSheet().addRule("body {white-space:pre-wrap}");
        }
    }

    protected View createView(Element elem, View view) {
        AttributeSet attrs = elem.getAttributes();
        if (attrs.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.DIV && supportsCollapsibility(attrs))
            return CollapsiblePanelFactory.createPanel(fromUser, this, elem, attrs);
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

    private static Color linkColor() {
        return JBUI.CurrentTheme.Link.Foreground.ENABLED;
    }
}
