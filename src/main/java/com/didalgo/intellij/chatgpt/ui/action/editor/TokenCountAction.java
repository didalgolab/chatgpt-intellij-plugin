/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action.editor;

import com.didalgo.gpt3.ModelType;
import com.didalgo.gpt3.TokenCount;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

public class TokenCountAction extends AnAction {

    public TokenCountAction() {
        super("Token Count", "Count tokens in the selected text or the entire content", null);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        // Enable the action only when an editor is available
        event.getPresentation().setEnabled(event.getDataContext().getData(CommonDataKeys.EDITOR) != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // Get the current editor and project
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        Project project = event.getProject();

        if (editor != null && project != null) {
            // Get the selected text or the entire content if no text is selected
            boolean inSelection = true;
            String text = editor.getSelectionModel().getSelectedText();
            if (text == null || text.isEmpty()) {
                text = editor.getDocument().getText();
                inSelection = false;
            }

            // Strip the text dropping leading and trailing whitespaces and newlines
            text = text.strip();

            // Count the number of tokens using TokenCount.getInstance().fromString(text)
            int tokenCount = TokenCount.fromString(text, ModelType.GPT_3_5_TURBO.getTokenizer());
            int lineCount = 1 + StringUtil.countNewLines(text);
            int charCount = text.length();

            String message = "<html><body><table>" +
                    "<tr><td style=\"text-align:right;padding:1\">Tokens:</td><td style=\"padding:1\">" + tokenCount + "</td></tr>" +
                    "<tr><td style=\"text-align:right;padding:1\">Lines:</td><td style=\"padding:1\">" + lineCount + "</td></tr>" +
                    "<tr><td style=\"text-align:right;padding:1\">Characters:</td><td style=\"padding:1\">" + charCount + "</td></tr>" +
                    "</table></body></html>";

            Messages.showMessageDialog(project, message, "Token Count", Messages.getInformationIcon());
        }
    }
}
