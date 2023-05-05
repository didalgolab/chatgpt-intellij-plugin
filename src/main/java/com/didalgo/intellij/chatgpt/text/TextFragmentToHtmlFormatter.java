/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.text;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;

import java.util.List;

@Service
public final class TextFragmentToHtmlFormatter implements TextFragmentFormatter {

    public static TextFragmentToHtmlFormatter getDefault() {
        return ApplicationManager.getApplication().getService(TextFragmentToHtmlFormatter.class);
    }

    private Parser markdownParser;
    private HtmlRenderer htmlRenderer;

    public Parser getMarkdownParser() {
        Parser parser = markdownParser;
        if (parser == null)
            parser = markdownParser = Parser.builder()
                    .extensions(List.of(TablesExtension.create()))
                    .build();

        return parser;
    }

    public HtmlRenderer getHtmlRenderer() {
        HtmlRenderer renderer = htmlRenderer;
        if (renderer == null)
            renderer = htmlRenderer = HtmlRenderer.builder()
                    .escapeHtml(true)
                    .softBreak("<br>")
                    .extensions(List.of(TablesExtension.create()))
                    .build();

        return renderer;
    }

    @Override
    public String format(TextFragment markdown) {
        Node document = getMarkdownParser().parse(markdown.markdown());
        return getHtmlRenderer().render(document);
    }
}
