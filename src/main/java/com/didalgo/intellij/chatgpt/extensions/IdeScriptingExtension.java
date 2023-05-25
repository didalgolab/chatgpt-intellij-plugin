/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.extensions;

import com.didalgo.intellij.chatgpt.ChatGptExtension;

public class IdeScriptingExtension implements ChatGptExtension {

    private static final String KEYWORD = "my ide";

    private static final String SYSTEM_PROMPT = """
            You are capable of using IntelliJ SDK API to virtually assist and help users with their questions or requests. When the user asks a question related to his IDE, you should determine whether
            you need to use IntelliJ SDK API to properly handle the requested task. If so, you will
            request the user to provide all the parameters you need, and then ask them to run the
            request for you. When you are ready to ask for a request, you should specify it using
            the following syntax:
                        
            <ide_script>
            ```java
            [relevant Java code leveraging IntelliJ Platform API]
            ```
            }</ide_script>
                        
            The user will then provide the response body, which you may use to
            formulate your answer. You should not respond with code, but rather provide an answer
            directly.""";

    @Override
    public boolean isEnabled(String prompt) {
        var lowerCasePrompt = prompt.toLowerCase();
        var keywordIndex = lowerCasePrompt.indexOf(KEYWORD);
        return keywordIndex == 0 || keywordIndex > 0 && Character.isLetter(keywordIndex - 1);
    }

    public String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public void appendSystemPrompt(StringBuilder prompt) {
        if (isEnabled(prompt.toString())) {
            if (!prompt.toString().endsWith("\n\n")) {
                prompt.append("\n\n");
            }
            prompt.append(SYSTEM_PROMPT);

        }
    }
}
