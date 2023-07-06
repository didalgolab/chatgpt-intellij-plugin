package com.didalgo.intellij.chatgpt.text;

@FunctionalInterface
public interface TextContent {

    StringBuilder appendTo(StringBuilder a);

    static String toString(TextContent content) {
        return content.appendTo(new StringBuilder()).toString();
    }
}
