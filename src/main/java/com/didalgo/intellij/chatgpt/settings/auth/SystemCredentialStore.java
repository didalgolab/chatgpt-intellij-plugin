/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.settings.auth;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.ide.passwordSafe.PasswordSafe;
import org.jetbrains.annotations.NotNull;

final class SystemCredentialStore implements CredentialStore {

    final static SystemCredentialStore INSTANCE = new SystemCredentialStore();

    @Override
    public String getPassword(String key) {
        return PasswordSafe.getInstance().getPassword(createCredentialAttributes(key));
    }

    @Override
    public String setAndGetPassword(String key, String password) {
        var credentialAttributes = createCredentialAttributes(key);
        var passwordSafe = PasswordSafe.getInstance();
        passwordSafe.setPassword(credentialAttributes, password);
        return passwordSafe.getPassword(credentialAttributes);
    }

    @NotNull
    private static CredentialAttributes createCredentialAttributes(@NotNull String keyspace) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("com.didalgo.ChatGPT", keyspace), null);
    }
}
