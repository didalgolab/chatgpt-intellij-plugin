package com.didalgo.intellij.chatgpt.ui.action.editor;

import com.didalgo.intellij.chatgpt.chat.ChatLink;
import com.didalgo.intellij.chatgpt.text.CodeFragmentFactory;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerEx;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FixCompilationErrorsAction extends AbstractEditorAction {

    private static final String DEFAULT_PROMPT = """
            Pretty please identify and resolve compilation {error|warning}(s) in the provided code snippet, highlighted using embedded `<Highlight>` tags.
            Review the highlighted {error|warning} descriptions for the compiler's feedback.
            Return the corrected code without {error|warning} highlights, or provide comprehensive guidance on further steps if you feel there is insufficient context to fix the {error|warning}(s).
            """;

    public FixCompilationErrorsAction() {
        super("Fix Compilation Errors", "Saving the day by magically fixing those pesky compilation errors or warnings", null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(true);
    }

    @Override
    protected void actionPerformed(Project project, Editor editor, String selectedText) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            ReadAction.run(() -> analyseHighlights(project, editor));
        });
    }

    @RequiresReadLock
    protected void analyseHighlights(Project project, Editor editor) {
        var severity = "error";
        var highlights = findCompilationInfos(project, editor, HighlightSeverity.ERROR);

        if (highlights.isEmpty()) {
            highlights = findCompilationInfos(project, editor, HighlightSeverity.WARNING);
            severity = "warning";
        }

        StringBuilder buf = new StringBuilder(editor.getDocument().getText());
        highlights.sort(Comparator.comparing(HighlightInfo::getEndOffset));

        addHighlightTagsToText(buf, highlights);

        ChatLink.forProject(project)
                .pushMessage(getPrompt(severity, highlights.size()),
                        List.of(CodeFragmentFactory.create(editor, buf.toString())));
    }

    private void addHighlightTagsToText(StringBuilder text, List<HighlightInfo> highlights) {
        Map<Integer, String> tags = new TreeMap<>();

        for (HighlightInfo highlight : highlights) {
            int endOffset = highlight.getEndOffset();
            int startOffset = highlight.getStartOffset();

            tags.put(endOffset, "</Highlight>");
            tags.put(startOffset, "<Highlight severity=\"" + highlight.getSeverity().getDisplayName()
                    + "\" description=\"" + highlight.getDescription() + "\">");
        }

        AtomicInteger delta = new AtomicInteger();
        tags.forEach((key, value) -> text.insert(key + delta.getAndAdd(value.length()), value));
    }

    private @NotNull List<HighlightInfo> findCompilationInfos(Project project, Editor editor, HighlightSeverity severity) {
        List<HighlightInfo> highlights = new ArrayList<>();
        DaemonCodeAnalyzerEx.processHighlights(
                editor.getDocument(), project, severity, 0, editor.getDocument().getTextLength(),
                highlights::add);
        return highlights;
    }

    private static String getPrompt(String severity, int numHighlights) {
        return DEFAULT_PROMPT.replace("{error|warning}", severity)
                .replace("(s)", numHighlights == 1 ? "" : "s");
    }
}
