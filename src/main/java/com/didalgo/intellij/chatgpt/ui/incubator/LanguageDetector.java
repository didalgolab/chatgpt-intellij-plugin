package com.didalgo.intellij.chatgpt.ui.incubator;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LanguageDetector {

    private static final Map<String, Pattern> languagePatterns = new HashMap<>();

    static {
        languagePatterns.put("Java", Pattern.compile("class\\s+[A-Za-z0-9_]+\\s*\\{"));
        languagePatterns.put("Python", Pattern.compile("def\\s+[A-Za-z0-9_]+\\s*\\("));
        languagePatterns.put("JavaScript", Pattern.compile("function\\s+[A-Za-z0-9_]+\\s*\\("));
        languagePatterns.put("C", Pattern.compile("#include\\s+<[A-Za-z0-9_]+\\.h>"));
        languagePatterns.put("C++", Pattern.compile("#include\\s+<[A-Za-z0-9_]+\\.hpp>"));
        languagePatterns.put("Ruby", Pattern.compile("def\\s+[A-Za-z0-9_]+\\s*"));
        languagePatterns.put("PHP", Pattern.compile("<\\?php"));
        // Add more patterns for other languages
    }

    public static String detectLanguage(String codeSnippet) {
        for (Map.Entry<String, Pattern> entry : languagePatterns.entrySet()) {
            if (entry.getValue().matcher(codeSnippet).find()) {
                return entry.getKey();
            }
        }
        return "Unknown";
    }

    public static void main(String[] args) {
        String javaCode = "public class HelloWorld {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "    }\n" +
                "}";

        String pythonCode = "def hello_world():\n" +
                "    print(\"Hello, World!\")\n" +
                "\n" +
                "hello_world()";

        System.out.println("Java code detected as: " + detectLanguage(javaCode));
        System.out.println("Python code detected as: " + detectLanguage(pythonCode));
    }
}