/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.models;

import com.didalgo.gpt3.ChatFormatDescriptor;
import com.didalgo.gpt3.GPT3Tokenizer;

public interface ModelType {

    String id();

    ModelFamily getFamily();

    int getInputTokenLimit();

    default GPT3Tokenizer getTokenizer() {
        try {
            return com.didalgo.gpt3.ModelType.forModel(id()).orElseThrow().getTokenizer();
        } catch (IllegalArgumentException e) {
            return com.didalgo.gpt3.ModelType.forModel(com.didalgo.gpt3.ModelType.GPT_4.modelName()).orElseThrow().getTokenizer();
        }
    }

    default ChatFormatDescriptor getChatFormatDescriptor() {
        try {
            return com.didalgo.gpt3.ModelType.forModel(id()).orElseThrow().getChatFormatDescriptor();
        } catch (IllegalArgumentException e) {
            return com.didalgo.gpt3.ModelType.forModel(com.didalgo.gpt3.ModelType.GPT_4.modelName()).orElseThrow().getChatFormatDescriptor();
        }
    }
}
