package com.didalgo.intellij.chatgpt.util;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.didalgo.intellij.chatgpt.util.StandardLanguage.*;
import static org.junit.jupiter.api.Assertions.*;

class StandardLanguageTest {

    @Test
    void findByIdentifier_gives_Language_identified_by_given_ID() {
        assertEquals(Optional.of(JAVA), findByIdentifier("java"));

        assertEquals(Optional.of(PYTHON), findByIdentifier("python"));
        assertEquals(Optional.of(PYTHON), findByIdentifier("py"));

        assertEquals(Optional.empty(), findByIdentifier("INVALID"));
    }
}