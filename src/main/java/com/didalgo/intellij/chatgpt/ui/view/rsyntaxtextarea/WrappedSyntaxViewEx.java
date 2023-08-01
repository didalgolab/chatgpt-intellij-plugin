package com.didalgo.intellij.chatgpt.ui.view.rsyntaxtextarea;

import org.fife.ui.rsyntaxtextarea.WrappedSyntaxView;

import javax.swing.text.Element;
import javax.swing.text.View;
import java.awt.*;

public class WrappedSyntaxViewEx extends WrappedSyntaxView {

    /**
     * Creates a new WrappedSyntaxView.  Lines will be wrapped
     * on character boundaries.
     *
     * @param elem the element underlying the view
     */
    public WrappedSyntaxViewEx(Element elem) {
        super(elem);
    }

    @Override
    public float getPreferredSpan(int axis) {
        if (axis != View.X_AXIS) {
            return super.getPreferredSpan(axis);
        }

        Component host = getContainer();
        FontMetrics metrics = host.getFontMetrics(host.getFont());
        return super.getPreferredSpan(axis) - metrics.charWidth('\u00b6');
    }
}
