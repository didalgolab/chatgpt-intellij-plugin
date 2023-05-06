package com.didalgo.intellij.chatgpt.util;

import java.util.List;

public enum StandardLanguageTypes {

    NONE(List.of(""), "text/plain", List.of()),
    ACTIONSCRIPT(List.of("actionscript"), "text/actionscript", List.of("as")),
    ASSEMBLER_X86(List.of("asm"), "text/asm", List.of("asm")),
    BBCODE(List.of("bbcode"), "text/bbcode", List.of("bb")),
    C(List.of("c"), "text/c", List.of("c", "h")),
    CLOJURE(List.of("clojure", "clj", "cljc", "cljx"), "text/clojure", List.of("clj")),
    CPLUSPLUS(List.of("cpp"), "text/cpp", List.of("cpp", "hpp")),
    CSHARP(List.of("csharp"), "text/cs", List.of("cs")),
    CSS(List.of("css"), "text/css", List.of("css")),
    CSV(List.of("csv"), "text/csv", List.of("csv")),
    D(List.of("d"), "text/d", List.of("d")),
    DOCKERFILE(List.of("dockerfile"), "text/dockerfile", List.of("dockerfile")),
    DART(List.of("dart"), "text/dart", List.of("dart")),
    DELPHI(List.of("delphi"), "text/delphi", List.of("pas")),
    DTD(List.of("dtd"), "text/dtd", List.of("dtd")),
    FORTRAN(List.of("fortran"), "text/fortran", List.of("f", "f90")),
    GO(List.of("go"), "text/golang", List.of("go")),
    GROOVY(List.of("groovy"), "text/groovy", List.of("groovy")),
    HANDLEBARS(List.of("handlebars"), "text/handlebars", List.of("hbs")),
    HOSTS(List.of("hosts"), "text/hosts", List.of("hosts")),
    HTACCESS(List.of("htaccess"), "text/htaccess", List.of(".htaccess")),
    HTML(List.of("html"), "text/html", List.of("html", "htm")),
    INI(List.of("ini"), "text/ini", List.of("ini")),
    JAVA(List.of("java"), "text/java", List.of("java")),
    JAVASCRIPT(List.of("javascript"), "text/javascript", List.of("js")),
    JSON(List.of("json"), "text/json", List.of("json")),
    JSON_WITH_COMMENTS(List.of("json_with_comments"), "text/jshintrc", List.of("jshintrc")),
    JSP(List.of("jsp"), "text/jsp", List.of("jsp")),
    KOTLIN(List.of("kotlin"), "text/kotlin", List.of("kt", "kts")),
    LATEX(List.of("latex"), "text/latex", List.of("tex")),
    LESS(List.of("less"), "text/less", List.of("less")),
    LISP(List.of("lisp"), "text/lisp", List.of("lisp")),
    LUA(List.of("lua"), "text/lua", List.of("lua")),
    MAKEFILE(List.of("makefile"), "text/makefile", List.of("makefile")),
    MARKDOWN(List.of("markdown"), "text/markdown", List.of("md")),
    MXML(List.of("mxml"), "text/mxml", List.of("mxml")),
    NSIS(List.of("nsis"), "text/nsis", List.of("nsi", "nsh")),
    PERL(List.of("perl"), "text/perl", List.of("pl", "pm")),
    PHP(List.of("php"), "text/php", List.of("php")),
    PROTO(List.of("proto"), "text/proto", List.of("proto")),
    PROPERTIES_FILE(List.of("properties_file"), "text/properties", List.of("properties")),
    PYTHON(List.of("python"), "text/python", List.of("py")),
    RUBY(List.of("ruby"), "text/ruby", List.of("rb")),
    SAS(List.of("sas"), "text/sas", List.of("sas")),
    SCALA(List.of("scala"), "text/scala", List.of("scala")),
    SQL(List.of("sql"), "text/sql", List.of("sql")),
    TCL(List.of("tcl"), "text/tcl", List.of("tcl")),
    TYPESCRIPT(List.of("typescript"), "text/typescript", List.of("ts")),
    UNIX_SHELL(List.of("unix_shell"), "text/unix", List.of("sh")),
    VISUAL_BASIC(List.of("visual_basic"), "text/vb", List.of("vb")),
    WINDOWS_BATCH(List.of("windows_batch"), "text/bat", List.of("bat", "cmd")),
    XML(List.of("xml"), "text/xml", List.of("xml")),
    YAML(List.of("yaml"), "text/yaml", List.of("yml", "yaml"));

    private final List<String> id;
    private final String mimeType;
    private final List<String> fileExtensions;

    StandardLanguageTypes(List<String> id, String mimeType, List<String> fileExtensions) {
        this.id = id;
        this.mimeType = mimeType;
        this.fileExtensions = fileExtensions;
    }

    public List<String> getId() {
        return id;
    }

    public String getMimeType() {
        return mimeType;
    }

    public List<String> getFileExtensions() {
        return fileExtensions;
    }

}