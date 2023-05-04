/* Copyright (c) 2023 Mariusz Bernacki <didalgo@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0 */
package com.didalgo.intellij.chatgpt.util;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;

import java.util.List;

public class HtmlUtil {

    private static Parser markdownParser;
    private static HtmlRenderer htmlRenderer;

    public static Parser getMarkdownParser() {
        Parser parser = markdownParser;
        if (parser == null)
            parser = markdownParser = Parser.builder()
                    .extensions(List.of(TablesExtension.create()))
                    .build();

        return parser;
    }

    public static HtmlRenderer getHtmlRenderer() {
        HtmlRenderer renderer = htmlRenderer;
        if (renderer == null)
            renderer = htmlRenderer = HtmlRenderer.builder()
                    .extensions(List.of(TablesExtension.create()))
                    .build();

        return renderer;
    }

    public static String md2html(String markdown) {
        Node document = getMarkdownParser().parse(markdown);
        return getHtmlRenderer().render(document);
    }
}
