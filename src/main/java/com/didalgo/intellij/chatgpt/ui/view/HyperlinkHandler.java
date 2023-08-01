/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.view;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.net.URI;
import java.util.function.Consumer;

public class HyperlinkHandler {

    public static void handleOrElse(HyperlinkEvent e, Consumer<HyperlinkEvent> orElse) {
        URI uri = URI.create(e.getDescription());
        HyperlinkListener listener;
        if ("assistant".equals(uri.getScheme())
                && e.getSource() instanceof Component source
                && (listener = findHyperlinkListenerInHierarchy(source)) != null) {
            listener.hyperlinkUpdate(e);
        } else {
            orElse.accept(e);
        }
    }

    protected static HyperlinkListener findHyperlinkListenerInHierarchy(Component comp) {
        for (Component c = comp; c != null; c = c.getParent())
            if (c instanceof JComponent jc
                    && jc.getClientProperty(HyperlinkListener.class) instanceof HyperlinkListener listener)
                return listener;

        return null;
    }

}
