package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.intellij.chatgpt.text.CodeFragment;

import java.util.Optional;

public interface InputContextEntry {

    Optional<CodeFragment> getCodeFragment();
}
