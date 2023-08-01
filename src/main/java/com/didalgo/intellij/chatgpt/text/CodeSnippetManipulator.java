/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeSnippetManipulator {

    private static final Pattern CODE_SNIPPET_BLOCK_PATTERN = Pattern.compile("(\\[(.*?)](?:<br>)?(?:\\s*(`{3,}).*?\\3)+)(?:<br>|\\s)*", Pattern.DOTALL);
    private static final Pattern LAST_PATH_PATTERN = Pattern.compile(".*[\\\\/](.*)$");

    public static String makeCodeSnippetBlocksCollapsible(String input) {
        Matcher matcher = CODE_SNIPPET_BLOCK_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            if (matcher.groupCount() < 2) {
                continue;
            }
            String fullMatch = matcher.group(1);
            String title = matcher.group(2);

            // Shorten the title
            String[] words = title.split("\\s+");
            String shortenedTitle = (words.length > 2 ? words[0] + " " + words[1] + " ... " : title);

            // Extract the last path
            Matcher pathMatcher = LAST_PATH_PATTERN.matcher(title);
            if (pathMatcher.matches()) {
                shortenedTitle += pathMatcher.group(1);
            }

            // Replace with the new div tag
            shortenedTitle = shortenedTitle.replace("\\", "\\\\").replace("$", "\\$");
            fullMatch = fullMatch.replace("\\", "\\\\").replace("$", "\\$");
            matcher.appendReplacement(sb, "<div class=\"collapsible\" ai-code-snippet title=\"" + shortenedTitle + "\">\n" + fullMatch + "\n</div>");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
