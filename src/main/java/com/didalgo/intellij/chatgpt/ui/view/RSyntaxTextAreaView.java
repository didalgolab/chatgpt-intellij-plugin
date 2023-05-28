/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;

import com.didalgo.intellij.chatgpt.util.Language;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.ui.AnActionButton;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.JBUI;
import com.vladsch.flexmark.util.sequence.Escaping;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.IOException;

public class RSyntaxTextAreaView extends ComponentView {

    private static final Logger log = LoggerFactory.getLogger(RSyntaxTextAreaView.class);
    private static Theme defaultTheme;

    private Language language;

    public RSyntaxTextAreaView(Element element, Language language) {
        super(element);
        this.language = language;
    }

    @Override
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.insertUpdate(e, a, f);
        updateText();
    }

    @Override
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.removeUpdate(e, a, f);
        updateText();
    }

    @Override
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.changedUpdate(e, a, f);
        updateText();
    }

    protected void updateText() {
        Component comp = getComponent();
        if (comp instanceof RTextScrollPane scrollPane && scrollPane.getTextArea() instanceof RSyntaxTextArea textArea)
            updateText(scrollPane, textArea);
    }

    protected void updateText(RTextScrollPane scrollPane, RSyntaxTextArea textArea) {
        try {
            textArea.setText(getText());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public String getText() throws BadLocationException {
        var element = getElement();
        var text = getDocument().getText(element.getStartOffset(), element.getEndOffset() - element.getStartOffset());
        //if (text.endsWith("\n"))
        //    text = text.substring(0, text.length() + (text.endsWith("\r\n")? -2: -1));

        text = Escaping.unescapeHtml(text);
        return text;
    }

    @Override
    protected Component createComponent() {
        try {
            return createComponent0();
        } catch (RuntimeException | Error e) {
            throw e;
        }
    }

    private static final DataKey<JTextArea> TEXT_AREA_KEY = DataKey.create("MyTextArea");

    protected static class MyRSyntaxTextArea extends RSyntaxTextArea implements DataProvider {

        @Override
        public @Nullable Object getData(@NotNull @NonNls String dataId) {
            if (TEXT_AREA_KEY.is(dataId))
                return this;
            return null;
        }
    }

    protected Component createComponent0() {
        RSyntaxTextArea textArea = new MyRSyntaxTextArea();
        textArea.setSyntaxEditingStyle(language.mimeType());
        textArea.setEditable(false);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAnimateBracketMatching(false);
        textArea.setAntiAliasingEnabled(false);
        textArea.setFractionalFontMetricsEnabled(false);
        textArea.setLineWrap(true);
        textArea.setSize(4000, 4000);
        textArea.setMarkOccurrences(true);
        textArea.setMarkOccurrencesDelay(500);
        Theme theme = getDefaultTheme();
        if (theme != null)
            theme.apply(textArea);

        RTextScrollPane scrollPane = new RTextScrollPane(textArea) {
            @Override
            public Dimension getPreferredSize() {
                Container cont = RSyntaxTextAreaView.this.getContainer();
                if (cont != null && (getWidth() == 0 || getWidth() > cont.getWidth())) {
                    setSize(RSyntaxTextAreaView.this.getContainer().getWidth(), Integer.MAX_VALUE / 2);
                }
                return textArea.getUI().getPreferredSize(this);
            }
        };
        scrollPane.setLineNumbersEnabled(false);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(6, 0, 5, 0));
        scrollPane.setWheelScrollingEnabled(false);
        scrollPane.setOpaque(false);
        for (MouseWheelListener listener : scrollPane.getMouseWheelListeners())
            scrollPane.removeMouseWheelListener(listener);

        Icon icon1 = IconUtil.scale(IconLoader.getIcon("/icons/expui/action/copy_dark.svg", RSyntaxTextArea.class), null, 1.25f);
        JButton copyAction = new JButton(icon1);
        copyAction.setMargin(JBUI.emptyInsets());
        copyAction.setCursor(new Cursor(Cursor.HAND_CURSOR));
        copyAction.setFocusable(false);

        ActionGroup actionGroup = new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                return new AnAction[] { new MyCopyAction(icon1) };
            }
        };

        Presentation presentation = new Presentation();
        presentation.setIcon(AllIcons.Actions.More);
        presentation.putClientProperty(ActionButton.HIDE_DROPDOWN_ICON, Boolean.TRUE);
        ActionButton myCorner = new ActionButton(actionGroup, presentation, ActionPlaces.UNKNOWN, new Dimension(20, 20)) {
            @Override
            protected DataContext getDataContext() {
                return DataManager.getInstance().getDataContext(this);
            }
        };
        myCorner.setNoIconsInPopup(true);

        JComponent corner = new CodeBlockActionPanel(textArea);

        textArea.add(corner);

        textArea.addComponentListener(new ComponentAdapter() {
            private static final int padding = 2;

            @Override
            public void componentResized(ComponentEvent e) {
                Component c = e.getComponent();
                if (corner.getHeight() <= 0) {
                    Dimension prefSize = corner.getPreferredSize();
                    corner.setSize(prefSize);
                }
                corner.setBounds(c.getWidth() - corner.getWidth() - padding, padding, corner.getWidth(), corner.getHeight());
            }
        });

        updateText(scrollPane, textArea);
        return scrollPane;
    }

    public static class CodeBlockActionPanel extends JPanel {
        public static Icon COPY_ICON_16x16_DARK = IconLoader.getIcon("/icons/expui/action/copy_dark.svg", RSyntaxTextArea.class);
        public static Icon COPY_ICON_22x22_DARK = IconUtil.scale(COPY_ICON_16x16_DARK, null, 1.25f);
        public static Icon INSERT_COPY_ICON_16x16_DARK = IconLoader.getIcon("/icons/expui/action/insert-copy_dark.svg", RSyntaxTextArea.class);
        public static Icon INSERT_COPY_ICON_22x22_DARK = IconUtil.scale(INSERT_COPY_ICON_16x16_DARK, null, 1.25f);

        private final RSyntaxTextArea textArea;

        public CodeBlockActionPanel(RSyntaxTextArea textArea) {
            super(new GridLayout(1, 0));
            this.textArea = textArea;
            setOpaque(false);
            createUI();
        }

        protected void createUI() {
            add(createActionButton(new MyCopyAction(COPY_ICON_16x16_DARK)));
            add(createActionButton(new MyInsertCodeAction(INSERT_COPY_ICON_16x16_DARK)));
        }

        protected ActionButton createActionButton(AnAction action) {
            Presentation presentation = new Presentation();
            presentation.setIcon(AllIcons.Actions.More);
            presentation.putClientProperty(ActionButton.HIDE_DROPDOWN_ICON, Boolean.TRUE);
            ActionButton actionButton = new ActionButton(action, action.getTemplatePresentation().clone(), ActionPlaces.UNKNOWN, new Dimension(20, 20)) {
                @Override
                protected DataContext getDataContext() {
                    return DataManager.getInstance().getDataContext(this);
                }
            };
            actionButton.setNoIconsInPopup(true);
            return actionButton;
        }
    }

    private static abstract class RSyntaxTextAreaAction extends AnActionButton {

        protected RSyntaxTextAreaAction(String text, String description, Icon icon) {
            super(text, description, icon);
        }

        protected JTextArea getTextArea(AnActionEvent event) {
            return event.getData(TEXT_AREA_KEY);
        }

        protected String getTextContent(JTextArea textArea) {
            String selectedText = textArea.getSelectedText();
            return (selectedText == null || selectedText.isEmpty())? textArea.getText() : selectedText;
        }
    }

    private static class MyCopyAction extends RSyntaxTextAreaAction {

        MyCopyAction(Icon icon) {
            super("", "Copy to Clipboard", icon);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            JTextArea textArea = e.getData(TEXT_AREA_KEY);
            if (textArea != null) {
                Transferable transferable = new StringSelection(getTextContent(textArea));
                CopyPasteManager.getInstance().setContents(transferable);
            }
        }
    }

    private static class MyInsertCodeAction extends RSyntaxTextAreaAction {

        MyInsertCodeAction(Icon icon) {
            super("", "Copy to Selected Text Editor", icon);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            if (project == null)
                return;

            JTextArea textArea = e.getData(TEXT_AREA_KEY);
            if (textArea == null)
                return;

            String targetText = getTextContent(textArea);
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(e.getProject());
            FileEditor[] editors = fileEditorManager.getSelectedEditors();

            for (FileEditor editor : editors) {
                if (editor instanceof TextEditor textEditor) {
                    Editor currentEditor = textEditor.getEditor();
                    SelectionModel selectionModel = currentEditor.getSelectionModel();
                    String selectedText = selectionModel.getSelectedText();

                    var document = currentEditor.getDocument();
                    var codeStyleManager = CodeStyleManager.getInstance(project);

                    // If selected text is not empty, replace selected text in the editor with the target text
                    if (selectedText != null && !selectedText.isEmpty()) {
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            int startOffset = selectionModel.getSelectionStart();
                            int endOffset = selectionModel.getSelectionEnd();
                            document.replaceString(startOffset, endOffset, targetText);
                            // Adjust the indentation of the inserted text
                            PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                            var psiFile = psiDocumentManager.getPsiFile(document);
                            if (psiFile != null) {
                                psiDocumentManager.commitDocument(document);
                                codeStyleManager.adjustLineIndent(psiFile, new TextRange(startOffset, startOffset + targetText.length()));
                            }
                        });
                    } else { // Otherwise, insert target text at the current cursor position
                        int caretOffset = currentEditor.getCaretModel().getOffset();
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            document.insertString(caretOffset, targetText);
                            // Update the selection after inserting the text
                            selectionModel.setSelection(caretOffset, caretOffset + targetText.length());
                            // Adjust the indentation of the inserted text
                            PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                            var psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
                            if (psiFile != null) {
                                psiDocumentManager.commitDocument(document);
                                codeStyleManager.adjustLineIndent(psiFile, new TextRange(caretOffset, caretOffset + targetText.length()));
                            }
                        });
                    }
                }
            }
        }
    }

    private Theme getDefaultTheme() {
        if (defaultTheme == null) {
            try {
                defaultTheme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
            } catch (IOException e) {
                log.warn("Unable to load RSyntaxTextArea theme due to " + e, e);
            }
        }
        return defaultTheme;
    }
}
