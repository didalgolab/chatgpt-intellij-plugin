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
import com.vladsch.flexmark.util.sequence.Escaping;

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
                    .softBreak("<br>")
                    .extensions(List.of(TablesExtension.create()))
                    .build();

        return renderer;
    }

    @Override
    public String format(TextFragment markdown) {
        String escaped = Escaping.escapeHtml(markdown.markdown(), false);
        Node document = getMarkdownParser().parse(escaped);
        String html = getHtmlRenderer().render(document);
        return unescapeCode(html);
    }

    private static String unescapeCode(String html) {
        StringBuilder result = new StringBuilder();
        int position = 0;
        int startCode, endCode;

        while ((startCode = html.indexOf("<code", position)) != -1) {
            startCode = html.indexOf(">", startCode);
            if (startCode == -1) {
                break;
            }

            endCode = html.indexOf("</code>", startCode);
            if (endCode == -1) {
                break;
            }

            result.append(html, position, startCode + 1);
            String codeFragment = html.substring(startCode + 1, endCode);
            result.append(Escaping.unescapeHtml(codeFragment));
            position = endCode;
        }

        result.append(html.substring(position));
        return result.toString();
    }
}
