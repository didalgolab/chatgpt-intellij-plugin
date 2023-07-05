package com.didalgo.intellij.chatgpt.text;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;

import javax.swing.*;
import java.awt.*;
import java.util.Stack;

public final class TextFragmentUtils {
    private  TextFragmentUtils() {}

    private static FlexmarkHtmlConverter htmlConverter;

    public static FlexmarkHtmlConverter getHtmlConverter() {
        if (htmlConverter == null)
            htmlConverter = FlexmarkHtmlConverter.builder().build();
        return htmlConverter;
    }

    public static TextFragment scrapContent(Component component) {
        Stack<Component> stack = new Stack<>();
        stack.push(component);
        StringBuilder html = new StringBuilder(), md = new StringBuilder();
        while (!stack.isEmpty()) {
            Component current = stack.pop();
            if (current instanceof JEditorPane editorPane) {
                String htmlText = editorPane.getText();
                String mdText = html2md(htmlText);
                html.append(htmlText).append("\n\n");
                md.append(mdText).append("\n\n");
            }

            if (current instanceof Container container) {
                Component[] children = container.getComponents();
                for (int i = children.length - 1; i >= 0; i--) {
                    stack.push(children[i]);
                }
            }
        }
        return TextFragment.of(md.toString().stripTrailing(), html.toString().stripTrailing());
    }

    public static String html2md(String html) {
        return getHtmlConverter().convert(html);
    }
}
