/* Copyright (c) 2023 Mariusz Bernacki <didalgo@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0 */
package com.didalgo.intellij.chatgpt.core;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.ZonedDateTime;

import com.intellij.openapi.project.Project;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class StandardTextSubstitutorTest {

    @Test
    void testResolvePlaceholders2() {
        var substitutor = new StandardTextSubstitutor(Mockito.mock(Project.class));
        var actualDateTime = ZonedDateTime.parse(substitutor.resolvePlaceholders("Current datetime: ${{NOW}}").substring(18), StandardTextSubstitutor.DATE_TIME_FORMATTER);
        assertTrue(Duration.between(ZonedDateTime.now(), actualDateTime).abs().compareTo(Duration.ofSeconds(5)) < 0, "Time difference is too large");
    }
}