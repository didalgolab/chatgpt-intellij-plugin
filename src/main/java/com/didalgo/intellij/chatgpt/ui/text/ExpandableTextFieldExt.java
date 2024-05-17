/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.text;

import com.didalgo.intellij.chatgpt.ui.PromptAttachmentHandler;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.textarea.TextComponentEditor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.Expandable;
import com.intellij.ui.components.JBScrollBar;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.Function;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.SwingUndoUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Objects;

import static com.didalgo.intellij.chatgpt.ui.text.NewlineFilter.NEWLINE_REPLACEMENT;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

public class ExpandableTextFieldExt extends ExtendableTextField implements Expandable, DataProvider {
    public static final DataKey<PromptAttachmentHandler> PROMPT_ATTACHMENT_HANDLER_KEY = DataKey.create("didalgo.ai.PromptAttachmentHandler");
    private static final KeyStroke SHIFT_ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
    private final ExpandableSupportExt<JTextComponent> support;
    private final Project project;
    private final PromptAttachmentHandler attachmentHandler;

    public ExpandableTextFieldExt(Project project, PromptAttachmentHandler attachmentHandler) {
        this(project, attachmentHandler,
                text -> StringUtil.split(text, NEWLINE_REPLACEMENT.toString(), true, false),
                lines -> String.join("\n", lines));
    }

    public ExpandableTextFieldExt(Project project, PromptAttachmentHandler attachmentHandler, @NotNull Function<? super String, ? extends List<String>> parser, @NotNull Function<? super List<String>, String> joiner) {
        this.project = project;
        this.attachmentHandler = attachmentHandler;
        Function<? super String, String> onShow = text -> StringUtil.join(parser.fun(text), "\n");
        Function<? super String, String> onHide = text -> joiner.fun(asList(/*MOD AGAINST IDEA CORE*/text.split("\r?\n")/*END MOD*/));
        support = new ExpandableSupportExt<>(this, onShow, onHide) {
            @NotNull
            @Override
            protected Content prepare(@NotNull JTextComponent field, @NotNull Function<? super String, @Nls String> onShow) {
                Font font = field.getFont();
                FontMetrics metrics = font == null ? null : field.getFontMetrics(font);
                int height = metrics == null ? 16 : metrics.getHeight();
                Dimension size = new Dimension(height * 32, height * 16);

                JTextArea area = createTextArea(onShow.fun(field.getText()), field.isEditable(), field.getBackground(), field.getForeground(), font);
                new MyShiftEnterAction(area).registerCustomShortcutSet(new CustomShortcutSet(SHIFT_ENTER), area);

                copyCaretPosition(field, area);

                JLabel label = createLabel(createCollapseExtension());
                label.setBorder(JBUI.Borders.empty(5, 0, 5, 5));

                JBScrollPane pane = new JBScrollPane(area);
                pane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
                pane.getVerticalScrollBar().add(JBScrollBar.LEADING, label);
                pane.getVerticalScrollBar().setBackground(area.getBackground());

                Insets insets = field.getInsets();
                Insets margin = field.getMargin();
                if (margin != null) {
                    insets.top += margin.top;
                    insets.left += margin.left;
                    insets.right += margin.right;
                    insets.bottom += margin.bottom;
                }

                JBInsets.addTo(size, insets);
                JBInsets.addTo(size, pane.getInsets());
                pane.setPreferredSize(size);
                pane.setViewportBorder(insets != null
                        ? createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right)
                        : createEmptyBorder());
                return new Content() {
                    @NotNull
                    @Override
                    public JComponent getContentComponent() {
                        return pane;
                    }

                    @Override
                    public JComponent getFocusableComponent() {
                        return area;
                    }

                    @Override
                    public void cancel(@NotNull Function<? super String, String> onHide) {
                        if (field.isEditable()) {
                            field.setText(onHide.fun(area.getText()));
                            copyCaretPosition(area, field);
                        }
                    }
                };
            }
        };
        setMonospaced(true);
        setExtensions(createExtensions());
    }

    @NotNull
    protected JTextArea createTextArea(@Nls @NotNull String text, boolean editable, Color background, Color foreground, Font font) {
        JTextArea area = new JTextArea(text);

        area.putClientProperty(Expandable.class, this);
        area.setEditable(editable);
        area.setBackground(background);
        area.setForeground(foreground);
        area.setFont(font);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);

        SwingUndoUtil.addUndoRedoActions(area);

        return area;
    }

    public void setMonospaced(boolean monospaced) {
        putClientProperty("monospaced", monospaced);
    }

    @Override
    public String getText() {
        return NewlineFilter.normalize(super.getText());
    }

    @Override
    public String getText(int offs, int len) throws BadLocationException {
        return NewlineFilter.normalize(super.getText(offs, len));
    }

    @Override
    public void setText(String text) {
        if (!Objects.equals(text, getText()))
            super.setText(NewlineFilter.denormalize(text));
    }

    @Override
    public String getSelectedText() {
        return NewlineFilter.normalize(super.getSelectedText());
    }

    @NotNull
    protected List<ExtendableTextComponent.Extension> createExtensions() {
        return (support == null)? emptyList(): singletonList(support.createExpandExtension());
    }

    public String getTitle() {
        return support.getTitle();
    }

    public void setTitle(@NlsContexts.PopupTitle String title) {
        support.setTitle(title);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled) support.collapse();
        super.setEnabled(enabled);
    }

    @Override
    public void collapse() {
        support.collapse();
    }

    @Override
    public boolean isExpanded() {
        return support.isExpanded();
    }

    @Override
    public void expand() {
        support.expand();
    }

    private static void copyCaretPosition(JTextComponent source, JTextComponent destination) {
        try {
            destination.setCaretPosition(source.getCaretPosition());
        }
        catch (Exception ignored) { }
    }

    private TextComponentEditor hostEditor;

    public TextComponentEditor getHostEditor() {
        if (hostEditor == null) {
            hostEditor = createHostEditor();
        }
        return hostEditor;
    }

    protected TextComponentEditor createHostEditor() {
        return new ExpandableTextComponentEditorImpl(project, this);
    }

    @Override
    public @Nullable Object getData(@NotNull @NonNls String dataId) {
        if (CommonDataKeys.HOST_EDITOR.is(dataId)) {
            return getHostEditor();
        }
        if (PROMPT_ATTACHMENT_HANDLER_KEY.is(dataId)) {
            return attachmentHandler;
        }
        return null;
    }

    public static class ExpandOnMultiLinePaste extends DocumentAdapter {
        private final Expandable expandable;

        public ExpandOnMultiLinePaste(Expandable expandable) {
            this.expandable = expandable;
        }

        @Override
        public void removeUpdate(@NotNull DocumentEvent e) { }

        @Override
        protected void textChanged(@NotNull DocumentEvent e) {
            try {
                String newText = e.getDocument().getText(e.getOffset(), e.getLength());
                if (newText.indexOf(NEWLINE_REPLACEMENT) >= 0 && !StringUtils.containsOnly(newText, NEWLINE_REPLACEMENT)) {
                    if (!expandable.isExpanded())
                        SwingUtilities.invokeLater(expandable::expand);
                }
            } catch (BadLocationException ignored) { }
        }
    }

    private final class MyShiftEnterAction extends DumbAwareAction {
        private final JTextArea area;

        private MyShiftEnterAction(JTextArea area) {
            this.area = area;
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(isExpanded());
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            int caretPosition = area.getCaretPosition();
            area.insert("\n", caretPosition);
            area.setCaretPosition(caretPosition + 1);
        }
    }
}
