package com.didalgo.intellij.chatgpt.chat;

import java.util.EventObject;

public class InputContextChangeEvent extends EventObject {

    public InputContextChangeEvent(InputContext source) {
        super(source);
    }

    @Override
    public InputContext getSource() {
        return (InputContext) super.getSource();
    }
}
