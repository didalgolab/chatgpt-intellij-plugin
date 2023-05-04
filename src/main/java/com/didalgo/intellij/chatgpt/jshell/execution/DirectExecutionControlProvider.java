/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.jshell.execution;

import jdk.jshell.execution.DirectExecutionControl;
import jdk.jshell.execution.LocalExecutionControlProvider;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionEnv;

import java.util.Map;

/**
 * A custom JShell ExecutionControlProvider that uses DirectExecutionControl to directly evaluate
 * expressions and statements in the same JVM as the JShell instance. This provides faster
 * evaluation than the default execution control, which creates a separate JVM for execution.
 * <p>
 * To use this custom provider, pass an instance of it to the JShell.Builder's executionEngine method.
 * <p>
 * Example:
 * <pre>
 * JShell jshell = JShell.builder()
 *         .executionEngine(new DirectExecutionControlProvider(), null)
 *         .build();
 * </pre>
 *
 * @author Mariusz Bernacki
 *
 */
public class DirectExecutionControlProvider extends LocalExecutionControlProvider {

    @Override
    public String name() {
        return "direct";
    }

    @Override
    public ExecutionControl generate(ExecutionEnv env, Map<String, String> parameters) {
        return new DirectExecutionControl();
    }
}
