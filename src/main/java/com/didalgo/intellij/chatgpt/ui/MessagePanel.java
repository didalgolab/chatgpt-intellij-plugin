/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import com.didalgo.intellij.chatgpt.text.TextFragment;
import com.didalgo.intellij.chatgpt.ui.view.RSyntaxTextAreaEnclosingView;
import com.didalgo.intellij.chatgpt.ui.view.RSyntaxTextAreaView;
import com.didalgo.intellij.chatgpt.ui.view.ViewUtils;
import com.didalgo.intellij.chatgpt.util.Language;
import com.didalgo.intellij.chatgpt.util.StandardLanguage;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.ExtendableHTMLViewFactory;
import com.intellij.util.ui.HTMLEditorKitBuilder;
import com.intellij.util.ui.HtmlPanel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.net.URI;
import java.util.Optional;

public class MessagePanel extends HtmlPanel {

    private volatile TextFragment text;

    public MessagePanel() {
        setEditorKit(new HTMLEditorKitBuilder()
                .withViewFactoryExtensions((e, v) -> createView(e, v), ExtendableHTMLViewFactory.Extensions.WORD_WRAP)
                .build());
        setOpaque(true);
        setBackground(JBColor.YELLOW);
    }

    protected View createView(Element elem, View view) {
        AttributeSet attrs = elem.getAttributes();
        Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
        Object o = (elementName != null) ? null : attrs.getAttribute(StyleConstants.NameAttribute);

        if (ViewUtils.containsSyntaxTextAreaView(view)) {
            return new RSyntaxTextAreaEnclosingView(view);
        }

        String elementText = getElementText(elem);
        if (elementText.equals("[Selected code]")) {

            JButton button = new JButton(elementText);
            button.setOpaque(false);
            button.setEnabled(false);
            button.setToolTipText("```java\n    aaaa\n```".replace("\n", "<br>").replace(" ", "&nbsp;"));
            //return new JButtonView(elem, button);
        }

        if (attrs.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.PRE) {
            String startTag = getCodeBlockStartTag(elem);
            if (startTag.equals("[Selected code]") || startTag.equals("[//]: # (Selected code)") || startTag.contains("Selected code")) {
                JButton button = new JButton(startTag);
                button.setOpaque(false);
                button.setEnabled(false);
                button.setToolTipText("```java\n    aaaa\n```".replace("\n", "<br>").replace(" ", "&nbsp;"));
                //return new JButtonView(elem, button);
            }

            return new RSyntaxTextAreaView(elem, getLanguage(elem).orElse(StandardLanguage.NONE));
        }

        for (int i = 0; i < view.getViewCount(); i++) {
            if (view.getView(i) instanceof RSyntaxTextAreaView) {
                "FOUND SYNTAXTESTAREA".toString();
            }
        }
        if (HTML.Tag.HTML.equals(o)) {
            "".toString();
        }

        return view;
    }

    private boolean isSelectedCode(String startTag) {
        return startTag.equals("[Selected code]");
    }

    private String getCodeBlockStartTag(Element block) {
        Element parent = block.getParentElement();
        try {
            for (int i = 1; i < parent.getElementCount(); i++)
                if (parent.getElement(i) == block) {
                    Element previousSibling = parent.getElement(i - 1);
                    Document document = previousSibling.getDocument();
                    return document.getText(previousSibling.getStartOffset(), previousSibling.getEndOffset()).strip();
                }
        } catch (BadLocationException ignore) {
            return "";
        }
        return "";
    }

    private String getElementText(Element elem) {
        try {
            Document document = elem.getDocument();
            return document.getText(elem.getStartOffset(), elem.getEndOffset()).strip();
        } catch (BadLocationException ignore) {
            return "";
        }
    }

    protected Optional<Language> getLanguage(Element elem) {
        String lang = getLanguageClassIfAvailable(elem);
        if (lang == null && elem.getElementCount() > 0)
            lang = getLanguageClassIfAvailable(elem.getElement(0));

        return StandardLanguage.findByIdentifier(lang);
    }

    protected String getLanguageClassIfAvailable(Element elem) {
        Element codeElement = null;
        AttributeSet codeAttrs;
        if (elem.getElementCount() > 0
                && (codeElement = elem.getElement(0)) != null
                && (codeAttrs = codeElement.getAttributes()) != null
                && codeAttrs.getAttribute(HTML.Tag.CODE) instanceof AttributeSet codeAttr
                && codeAttr.getAttribute(HTML.Attribute.CLASS) instanceof String codeLang
                && codeLang.startsWith("language-")) {
            return codeLang.substring("language-".length());
        }
        return null;
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
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            URI uri = URI.create(e.getDescription());
            HyperlinkListener listener;
            if ("assistant".equals(uri.getScheme())
                    && (listener = findHyperlinkListenerInHierarchy()) != null) {
                listener.hyperlinkUpdate(e);
            } else {
                super.hyperlinkUpdate(e);
            }
        }
    }

    private HyperlinkListener findHyperlinkListenerInHierarchy() {
        for (Component c = this; c != null; c = c.getParent())
            if (c instanceof JComponent jc
                    && jc.getClientProperty(HyperlinkListener.class) instanceof HyperlinkListener listener)
                return listener;

        return null;
    }

    public void updateMessage(TextFragment updateMessage) {
        this.text = updateMessage;
        update();
    }
}
