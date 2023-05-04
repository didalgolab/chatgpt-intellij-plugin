/* Copyright (c) 2023 Mariusz Bernacki <didalgo@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0 */
package com.didalgo.intellij.chatgpt.text.encryption;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AESTest {

    @SuppressWarnings("UnnecessaryUnicodeEscape")
    @Test
    void testEncryptAndDecrypt() throws Exception {
        var PLAIN_TEXT = "\u1F51The password to the Pandora's Box, the secrets within which are better left undiscovered...";
        var encryptedText = AES.encrypt(PLAIN_TEXT);
        var decryptedText = AES.decrypt(encryptedText);
        assertEquals(PLAIN_TEXT, decryptedText);
    }

    @Test
    void encrypt_passes_through_empty_String() throws Exception {
        assertEquals("", AES.encrypt(""));
        assertEquals("", AES.decrypt(""));
    }
}