/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import com.didalgo.intellij.chatgpt.text.TextFragment;
import com.didalgo.intellij.chatgpt.util.Language;
import com.didalgo.intellij.chatgpt.util.StandardLanguage;
import com.intellij.util.ui.ExtendableHTMLViewFactory;
import com.intellij.util.ui.HTMLEditorKitBuilder;
import com.intellij.util.ui.HtmlPanel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.*;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.util.Optional;

public class MessagePanel extends HtmlPanel {

    private volatile TextFragment text;

    public MessagePanel() {
        setEditorKit(new HTMLEditorKitBuilder()
                .withViewFactoryExtensions((e, v) -> createView(e, v), ExtendableHTMLViewFactory.Extensions.WORD_WRAP)
                .build());
    }

    protected View createView(Element elem, View view) {
        AttributeSet attrs = elem.getAttributes();
        Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
        Object o = (elementName != null) ? null : attrs.getAttribute(StyleConstants.NameAttribute);

        if (attrs.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.PRE) {
            return new RSyntaxTextAreaComponentView(elem, getLanguage(elem).orElse(StandardLanguage.NONE));
        }

        return view;
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

    public void updateMessage(TextFragment updateMessage) {
        this.text = updateMessage;
        update();
    }
}
