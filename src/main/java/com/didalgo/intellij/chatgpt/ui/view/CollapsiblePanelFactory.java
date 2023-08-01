/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.view;

import com.didalgo.intellij.chatgpt.ui.MessagePanel;
import com.didalgo.intellij.chatgpt.ui.MessageRenderer;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.io.IOException;
import java.io.StringWriter;

public class CollapsiblePanelFactory {
    private static final String HONOR_COLLAPSIBLE_PANELS = "Factory.HonorCollapsiblePanels";


    public static boolean supportsCollapsibility(MessageRenderer renderer, AttributeSet attrs) {
        return "collapsible".equals(attrs.getAttribute(HTML.Attribute.CLASS))
                && !Boolean.FALSE.equals(renderer.getClientProperty(HONOR_COLLAPSIBLE_PANELS));
    }

    @NotNull
    public static JComponentView createPanel(MessageRenderer renderer, Element elem, AttributeSet attrs) {
        JXCollapsiblePane collapsiblePane = new JXCollapsiblePane();
        collapsiblePane.setCollapsed(true);

        JEditorPane contentPane = new MessagePanel();
        contentPane.putClientProperty(HONOR_COLLAPSIBLE_PANELS, Boolean.FALSE);
        // Get the content of the Element object
        StringWriter out = new StringWriter();
        try {
            int startOffset = elem.getStartOffset();
            int length = elem.getEndOffset() - startOffset;
            renderer.getEditorKit().write(out, elem.getDocument(), elem.getStartOffset(), length);
        } catch (IOException | BadLocationException ignore) {

        }

        // Set the content of the JTextPane in HTML format
        contentPane.setContentType("text/html; charset=UTF-8");
        contentPane.setText(out.toString());
        contentPane.setOpaque(false);
        contentPane.setBorder(null);
        contentPane.setEditable(false);

        JButton toggleButton = new JButton(collapsiblePane.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION));
        toggleButton.setText("" + attrs.getAttribute(HTML.Attribute.TITLE));
        toggleButton.setOpaque(false);
        toggleButton.setEnabled(true);
        toggleButton.setFocusable(false);
        toggleButton.addActionListener(event -> {
            if (contentPane.getParent() == null)
                collapsiblePane.add(contentPane);
        });

        JPanel viewPanel = new JPanel(new BorderLayout()) {
            @Override
            public void reshape(int x, int y, int w, int h) {
                w = Math.min(w, renderer.getWidth() - 2);
                super.reshape(x, y, w, h);
            }
        };
        viewPanel.setOpaque(false);
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        togglePanel.setOpaque(false);
        togglePanel.add(toggleButton);
        viewPanel.add(togglePanel, BorderLayout.NORTH);
        viewPanel.add(collapsiblePane, BorderLayout.CENTER);
        return new JComponentView(elem, viewPanel);
    }
}
