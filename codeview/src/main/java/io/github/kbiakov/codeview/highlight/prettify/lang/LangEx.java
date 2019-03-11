package io.github.kbiakov.codeview.highlight.prettify.lang;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import io.github.kbiakov.codeview.highlight.prettify.parser.Prettify;

public class LangEx extends Lang {

    public LangEx() {
        List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
        List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^[\\t\\n\\r \\xA0]+"), null, "\t\n\r \\xA0"}));
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^#.*"), null, "#"}));
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^'(?:[^'\\\\]|\\\\(?:.|\\n|\\r))*'?"), null, "\\'"}));
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_ATTRIB_NAME, Pattern.compile("^@\\w+"), null, "@"}));
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^[!%&()*+,\\-;<=>?\\[\\\\\\]^{|}]+"), null, "!%&()*+,-;<=>?[\\\\]^{|}"}));
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^(?:0o[0-7](?:[0-7]|_[0-7])*|0x[\\da-fA-F](?:[\\da-fA-F]|_[\\da-fA-F])*|\\d(?:\\d|_\\d)*(?:\\.\\d(?:\\d|_\\d)*)?(?:[eE][+\\-]?\\d(?:\\d|_\\d)*)?)"),
        null, "0123456789"}));

        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_ATTRIB_NAME, Pattern.compile("^iex(?:\\(\\d+\\))?> ")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^::"), null}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^:(?:\\w+[\\!\\?\\@]?|\"(?:[^\"\\\\]|\\\\.)*\"?)")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_ATTRIB_NAME, Pattern.compile("^(?:__(?:CALLER|ENV|MODULE|DIR)__)")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^(?:alias|case|catch|def(?:delegate|exception|impl|macrop?|module|overridable|p?|protocol|struct)|do|else|end|fn|for|if|in|import|quote|raise|require|rescue|super|throw|try|unless|unquote(?:_splicing)?|use|when|with|yield)\\b")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^(?:true|false|nil)\\b")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^(?:\\w+[\\!\\?\\@]?|\"(?:[^\"\\\\]|\\\\.)*\"):(?!:)")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^\"\"\"\\s*(\\r|\\n)+(?:\"\"?(?!\")|[^\\\\\"]|\\\\(?:.|\\n|\\r))*\"{0,3}")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^\"(?:[^\"\\\\]|\\\\(?:.|\\n|\\r))*\"?(?!\")")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_TYPE, Pattern.compile("^[A-Z]\\w*")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^_\\w*")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^[$a-z]\\w*[\\!\\?]?")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_ATTRIB_VALUE, Pattern.compile("^~[A-Z](?:\\/(?:[^\\/\\r\\n\\\\]|\\\\.)+\\/|\\|(?:[^\\|\\r\\n\\\\]|\\\\.)+\\||\"(?:[^\"\\r\\n\\\\]|\\\\.)+\"|'(?:[^'\\r\\n\\\\]|\\\\.)+')[A-Z]*", Pattern.CASE_INSENSITIVE)}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_ATTRIB_VALUE, Pattern.compile("^~[A-Z](?:\\((?:[^\\)\\r\\n\\\\]|\\\\.)+\\)|\\[(?:[^\\]\\r\\n\\\\]|\\\\.)+\\]|\\{(?:[^\\}\\r\\n\\\\]|\\\\.)+\\}|\\<(?:[^\\>\\r\\n\\\\]|\\\\.)+\\>)[A-Z]*", Pattern.CASE_INSENSITIVE)}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^(?:\\.+|\\/|[:~])")}));


        setShortcutStylePatterns(_shortcutStylePatterns);
        setFallthroughStylePatterns(_fallthroughStylePatterns);
    }

    public static List<String> getFileExtensions() {
        return Arrays.asList(new String[]{"ex", "exs"});
    }
}
