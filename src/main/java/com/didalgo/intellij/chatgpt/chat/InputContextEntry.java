package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.intellij.chatgpt.text.TextContent;

import java.util.Optional;

public interface InputContextEntry {

    Optional<TextContent> getTextContent();
}
