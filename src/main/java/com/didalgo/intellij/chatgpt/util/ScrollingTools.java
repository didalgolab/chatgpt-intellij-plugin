/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.util;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Scrolls the pane down when size changes, if it was scrolled down before.
 *
 * If not scrolled to the bottom (i.e. last pixel unseen), scrolling remains unchanged.
 */
public class ScrollingTools {

    private static final int AUTOSCROLL_EDGE_LEEWAY = 20;

    public static void installAutoScrollToBottom(JScrollPane scrollPane) {
        final AtomicBoolean wasScrolledToBottom = new AtomicBoolean(true);

        JViewport viewport = scrollPane.getViewport();
        Component view = viewport.getView();

        ComponentAdapter scrollToBottomListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (wasScrolledToBottom.get()) {
                    scrollToBottom(scrollPane);
                }
            }
        };

        viewport.addComponentListener(scrollToBottomListener);
        view.addComponentListener(scrollToBottomListener);

        scrollPane.getVerticalScrollBar().getModel().addChangeListener(new ChangeListener() {
            int oldValue = scrollPane.getVerticalScrollBar().getValue();

            @Override
            public void stateChanged(ChangeEvent e) {
                int newValue = scrollPane.getVerticalScrollBar().getValue();
                if (oldValue != newValue) {
                    oldValue = newValue;
                    wasScrolledToBottom.set(scrollPane.getVerticalScrollBar().getValue() + viewport.getViewRect().height >= view.getHeight() - AUTOSCROLL_EDGE_LEEWAY);
                }
            }
        });
    }

    public static void scrollToBottom(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setValue(Integer.MAX_VALUE/2);
    }
}
