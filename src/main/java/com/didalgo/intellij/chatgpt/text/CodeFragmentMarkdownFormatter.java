/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.text;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;

@Service
public final class CodeFragmentMarkdownFormatter implements CodeFragmentFormatter {

    public static CodeFragmentMarkdownFormatter getDefault() {
        return ApplicationManager.getApplication().getService(CodeFragmentMarkdownFormatter.class);
    }

    @Override
    public String format(CodeFragment cf) {
        if (cf.isEmpty()) {
            return "";
        }
        String content = cf.content();
        String fenceBlockDelim = determineMinimumFenceBlockDelimiter(content);

        StringBuilder sb = new StringBuilder();
        sb.append(fenceBlockDelim).append(cf.language()).append('\n');
        sb.append(content).append('\n');
        sb.append(fenceBlockDelim);

        // TODO: Everything below is already rendered just before fenced block as its title.
        //String description = cf.description();
        //if (!description.isEmpty()) {
        //    sb.append('\n').append('_').append(Escaping.escapeMarkdown(description)).append('_');
        //}

        return sb.toString();
    }

    private String determineMinimumFenceBlockDelimiter(String content) {
        StringBuilder sb = new StringBuilder("```");
        while (content.contains(sb)) {
            sb.append("`");
        }
        return sb.toString();
    }
}
