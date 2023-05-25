/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.view;

import javax.swing.*;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import java.awt.*;

public class JButtonView extends ComponentView {
    private JButton button;

    /**
     * Creates a new JButtonView object.
     *
     * @param elem the element to decorate
     * @param button the JButton to be used in this view
     */
    public JButtonView(Element elem, JButton button) {
        super(elem);
        this.button = button;
    }

    /**
     * Create the component that is associated with
     * this view.  This will be called when it has
     * been determined that a new component is needed.
     * This would result from a call to setParent or
     * as a result of being notified that attributes
     * have changed.
     * @return the component that is associated with
     * this view
     */
    @Override
    protected Component createComponent() {
        return button;
    }

}