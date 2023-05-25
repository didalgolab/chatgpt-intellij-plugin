/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.view;

import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import java.awt.*;

public class ProxiedView extends View {
    protected final View delegate;

    public ProxiedView(View delegate) {
        super(delegate.getElement());
        this.delegate = delegate;
    }

    @Override
    public View getParent() {
        return delegate.getParent();
    }

    @Override
    public boolean isVisible() {
        return delegate.isVisible();
    }

    @Override
    public float getPreferredSpan(int axis) {
        return delegate.getPreferredSpan(axis);
    }

    @Override
    public float getMinimumSpan(int axis) {
        return delegate.getMinimumSpan(axis);
    }

    @Override
    public float getMaximumSpan(int axis) {
        return delegate.getMaximumSpan(axis);
    }

    @Override
    public void preferenceChanged(View child, boolean width, boolean height) {
        delegate.preferenceChanged(child, width, height);
    }

    @Override
    public float getAlignment(int axis) {
        return delegate.getAlignment(axis);
    }

    @Override
    public void paint(Graphics g, Shape allocation) {
        delegate.paint(g, allocation);
    }

    @Override
    public void setParent(View parent) {
        delegate.setParent(parent);
    }

    @Override
    public int getViewCount() {
        return delegate.getViewCount();
    }

    @Override
    public View getView(int n) {
        return delegate.getView(n);
    }

    @Override
    public void removeAll() {
        delegate.removeAll();
    }

    @Override
    public void remove(int i) {
        delegate.remove(i);
    }

    @Override
    public void insert(int offs, View v) {
        delegate.insert(offs, v);
    }

    @Override
    public void append(View v) {
        delegate.append(v);
    }

    @Override
    public void replace(int offset, int length, View[] views) {
        delegate.replace(offset, length, views);
    }

    @Override
    public int getViewIndex(int pos, Position.Bias b) {
        return delegate.getViewIndex(pos, b);
    }

    @Override
    public Shape getChildAllocation(int index, Shape a) {
        return delegate.getChildAllocation(index, a);
    }

    @Override
    public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet) throws BadLocationException {
        return delegate.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
    }

    @Override
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        return delegate.modelToView(pos, a, b);
    }

    @Override
    public Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape a) throws BadLocationException {
        return delegate.modelToView(p0, b0, p1, b1, a);
    }

    @Override
    public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
        return delegate.viewToModel(x, y, a, biasReturn);
    }

    @Override
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        delegate.insertUpdate(e, a, f);
    }

    @Override
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        delegate.removeUpdate(e, a, f);
    }

    @Override
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        delegate.changedUpdate(e, a, f);
    }

    @Override
    public Document getDocument() {
        return delegate.getDocument();
    }

    @Override
    public int getStartOffset() {
        return delegate.getStartOffset();
    }

    @Override
    public int getEndOffset() {
        return delegate.getEndOffset();
    }

    @Override
    public Element getElement() {
        return delegate.getElement();
    }

    @Override
    public Graphics getGraphics() {
        return delegate.getGraphics();
    }

    @Override
    public AttributeSet getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public View breakView(int axis, int offset, float pos, float len) {
        return delegate.breakView(axis, offset, pos, len);
    }

    @Override
    public View createFragment(int p0, int p1) {
        return delegate.createFragment(p0, p1);
    }

    @Override
    public int getBreakWeight(int axis, float pos, float len) {
        return delegate.getBreakWeight(axis, pos, len);
    }

    @Override
    public int getResizeWeight(int axis) {
        return delegate.getResizeWeight(axis);
    }

    @Override
    public void setSize(float width, float height) {
        delegate.setSize(width, height);
    }

    @Override
    public Container getContainer() {
        return delegate.getContainer();
    }

    @Override
    public ViewFactory getViewFactory() {
        return delegate.getViewFactory();
    }

    @Override
    public String getToolTipText(float x, float y, Shape allocation) {
        return delegate.getToolTipText(x, y, allocation);
    }

    @Override
    public int getViewIndex(float x, float y, Shape allocation) {
        return delegate.getViewIndex(x, y, allocation);
    }

    @Override
    @Deprecated
    public Shape modelToView(int pos, Shape a) throws BadLocationException {
        return delegate.modelToView(pos, a);
    }

    @Override
    @Deprecated
    public int viewToModel(float x, float y, Shape a) {
        return delegate.viewToModel(x, y, a);
    }
}