package com.didalgo.intellij.chatgpt.ui.view.rsyntaxtextarea;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaUI;
import org.fife.ui.rsyntaxtextarea.WrappedSyntaxView;

import javax.swing.*;
import javax.swing.text.Element;
import javax.swing.text.View;

public class RSyntaxTextAreaUIEx extends RSyntaxTextAreaUI {

    /**
     * Constructor.
     *
     * @param rSyntaxTextArea The text area.
     */
    public RSyntaxTextAreaUIEx(JComponent rSyntaxTextArea) {
        super(rSyntaxTextArea);
    }

    @Override
    public View create(Element elem) {
        View view = super.create(elem);
        if (view instanceof WrappedSyntaxView)
            view = new WrappedSyntaxViewEx(view.getElement());

        return view;
    }
}
