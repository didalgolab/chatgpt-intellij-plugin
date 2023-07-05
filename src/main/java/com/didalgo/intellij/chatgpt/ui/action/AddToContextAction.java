package com.didalgo.intellij.chatgpt.ui.action;

import com.didalgo.intellij.chatgpt.chat.ChatLink;
import com.didalgo.intellij.chatgpt.text.TextFragment;
import com.didalgo.intellij.chatgpt.text.TextFragmentUtils;
import com.didalgo.intellij.chatgpt.ui.context.stack.CodeFragmentInfo;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class AddToContextAction extends AnAction {

    private static final DataKey<JBPopup> DOCUMENTATION_POPUP_KEY = DataKey.create("documentation.popup");

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        JBPopup popup = e.getData(DOCUMENTATION_POPUP_KEY);
        if (popup != null) {
            Project project = e.getProject();
            if (project != null) {
                TextFragment textFragment = TextFragmentUtils.scrapContent(popup.getContent());

                var icon = AllIcons.Actions.ShowAsTree;
                var text = "[" + LocalDateTime.now().withNano(0) + "]";
                CodeFragmentInfo info = new CodeFragmentInfo(icon, text, textFragment);

                ChatLink.forProject(project).getInputContext().addEntry(info);
            }
        }
    }
}
