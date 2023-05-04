/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.jshell;

import com.didalgo.intellij.chatgpt.jshell.execution.DirectExecutionControlProvider;
import jdk.jshell.Diag;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class JShellHandle {

    private final JShell jShell;

    public JShellHandle() {
        jShell = JShell.builder()
                .executionEngine(new DirectExecutionControlProvider(), null)
                .build();
    }

    public final JShell getJShell() {
        return jShell;
    }

    public List<SnippetEvent> eval(String input) {
        return getJShell().eval(input);
    }

    public synchronized List<SnippetEvent> setVariables(Map<String, Object> vars) {
        List<SnippetEvent> resultList = new ArrayList<>();
        var jShell = getJShell();
        try {
            vars = new HashMap<>(vars);
            Variables.VALUES.set(vars);
            vars.forEach((var, value) -> {
                var varType = value.getClass().getCanonicalName();
                resultList.addAll(jShell.eval(varType + ' ' + var + " = " + Variables.valueSnippet(var)));
            });
        } finally {
            Variables.VALUES.set(null);
        }
        return resultList;
    }

    public static class Variables {
        private static final AtomicReference<Map<String, Object>> VALUES = new AtomicReference<>();

        @SuppressWarnings("unchecked")
        public static <T> T value(String varName) {
            var values = VALUES.get();
            return (values == null)? null: (T)values.get(varName);
        }

        public static String valueSnippet(String varName) {
            return Variables.class.getCanonicalName() + ".value(\"" + varName + "\");";
        }
    }

    public static void main(String[] args) {
        JShellHandle handle = new JShellHandle();
        List<SnippetEvent> events = handle.setVariables(Map.of("y", 123));

        for (SnippetEvent event : events) {
            if (event.status().isActive()) {
                System.out.println(event.status() + ": " + event.value());
            } else {
                System.out.println("Snippet Error: " + event.status());
                List<Diag> diagnostics = handle.getJShell().diagnostics(event.snippet()).toList();
                for (Diag diag : diagnostics) {
                    System.out.println("  " + diag.getMessage(Locale.ENGLISH));
                }
            }
        }
    }
}
