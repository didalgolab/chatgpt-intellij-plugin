/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.incubator;

import com.didalgo.intellij.chatgpt.jshell.JShellHandle;
import com.didalgo.intellij.chatgpt.jshell.execution.DirectExecutionControlProvider;
import jdk.jshell.Diag;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JShellExample {

    public static void main(String[] args) {
        //String codeSnippet = "new Exception().printStackTrace();"; // This line is the expression that will be evaluated and printed
        String codeSnippet = "x*y;";

        JShellHandle shellHandle = new JShellHandle();
        shellHandle.setVariables(Map.of("x", 20, "y", 10L));

        JShellExecutionHelper.executeCode(codeSnippet, shellHandle);
    }

    public static class JShellExecutionHelper {

        public static void executeCode(String code, JShellHandle handle) {
            try (JShell __jshell = JShell.builder()
                    .executionEngine(new DirectExecutionControlProvider(), null)
                    .build())
            {
                List<SnippetEvent> events = handle.getJShell().eval(code);

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
    }
}
