/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import com.didalgo.intellij.chatgpt.text.TextFragment;
import com.didalgo.intellij.chatgpt.ui.view.*;
import com.didalgo.intellij.chatgpt.util.StandardLanguage;
import com.intellij.util.ui.ExtendableHTMLViewFactory;
import com.intellij.util.ui.HTMLEditorKitBuilder;
import com.intellij.util.ui.HtmlPanel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import java.awt.*;

public class MessagePanel extends HtmlPanel implements MessageRenderer {

    private volatile TextFragment text;

    public MessagePanel() {
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
}
