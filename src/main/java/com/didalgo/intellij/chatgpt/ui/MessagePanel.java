/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import com.didalgo.intellij.chatgpt.text.TextFragment;
import com.intellij.util.ui.ExtendableHTMLViewFactory;
import com.intellij.util.ui.HTMLEditorKitBuilder;
import com.intellij.util.ui.HtmlPanel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.*;
import javax.swing.text.html.HTML;
import java.awt.*;

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
            return new RSyntaxTextAreaComponentView(elem);
        }

        return view;
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
