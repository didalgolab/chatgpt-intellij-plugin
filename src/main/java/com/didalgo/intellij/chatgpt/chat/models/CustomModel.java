package com.didalgo.intellij.chatgpt.chat.models;

public record CustomModel(
        String id,
        ModelFamily family,
        int inputTokenLimit
) implements ModelType {

    @Override
    public ModelFamily getFamily() {
        return family;
    }

    @Override
    public int getInputTokenLimit() {
        return inputTokenLimit;
    }
}
