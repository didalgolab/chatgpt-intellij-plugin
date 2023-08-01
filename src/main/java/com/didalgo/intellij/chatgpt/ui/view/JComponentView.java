/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.view;

import javax.swing.*;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import java.awt.*;

public class JComponentView extends ComponentView {
    private final JComponent component;

    /**
     * Creates a new JComponentView object.
     *
     * @param elem     the element to decorate
     * @param component the JComponent to be used in this view
     */
    public JComponentView(Element elem, JComponent component) {
        super(elem);
        this.component = component;
    }

    /**
     * Create the component that is associated with
     * this view.  This will be called when it has
     * been determined that a new component is needed.
     * This would result from a call to setParent or
     * as a result of being notified that attributes
     * have changed.
     *
     * @return the component that is associated with
     * this view
     */
    @Override
    protected Component createComponent() {
        return component;
    }
}