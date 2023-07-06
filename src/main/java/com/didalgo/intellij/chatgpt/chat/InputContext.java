package com.didalgo.intellij.chatgpt.chat;

import java.util.List;

public interface InputContext {

    void addListener(InputContextListener listener);

    void removeListener(InputContextListener listener);

    void addEntry(InputContextEntry newEntry);

    void removeEntry(InputContextEntry newEntry);

    List<InputContextEntry> getEntries();

    boolean isEmpty();

    void clear();

}
