package com.didalgo.intellij.chatgpt.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextFragmentToHtmlFormatterTest {

    TextFragmentToHtmlFormatter formatter = new TextFragmentToHtmlFormatter();

    @Test
    void format_gives_correct_soft_newlines_in_html() {
        TextFragment fragment = TextFragment.of("My text\n\nwith\nnewlines.");
        String expectedHtml = "<p>My text</p>\n<p>with<br>newlines.</p>\n";
        String actualHtml = formatter.format(fragment);
        assertEquals(expectedHtml, actualHtml);
    }
}