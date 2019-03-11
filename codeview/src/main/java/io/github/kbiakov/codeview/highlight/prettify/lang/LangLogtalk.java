package io.github.kbiakov.codeview.highlight.prettify.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import io.github.kbiakov.codeview.highlight.prettify.parser.Prettify;

public class LangLogtalk extends Lang {

    public LangLogtalk() {
        List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
        List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^\\\"(?:[^\\\"\\\\\\n\\x0C\\r]|\\\\[\\s\\S])*(?:\\\"|$)"), null, "\""}));
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^[a-z][a-zA-Z0-9_]*")}));
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^\\'(?:[^\\'\\\\\\n\\x0C\\r]|\\\\[^&])+\\'?"), null, "\'"}));
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^(?:0'.|0b[0-1]+|0o[0-7]+|0x[\\da-f]+|\\d+(?:\\.\\d+)?(?:e[+\\-]?\\d+)?)", Pattern.CASE_INSENSITIVE), null, "0123456789"}));

        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^%[^\\r\\n]*"), null, "%"}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^\\/\\*[\\s\\S]*?\\*\\/")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^\\s*:-\\s(c(a(lls|tegory)|oinductive)|p(ublic|r(ot(ocol|ected)|ivate))|e(l(if|se)|n(coding|sure_loaded)|xport)|i(f|n(clude|itialization|fo))|alias|d(ynamic|iscontiguous)|m(eta_(non_terminal|predicate)|od(e|ule)|ultifile)|reexport|s(et_(logtalk|prolog)_flag|ynchronized)|o(bject|p)|use(s|_module))")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^\\s*:-\\s(e(lse|nd(if|_(category|object|protocol)))|built_in|dynamic|synchronized|threaded)")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_TYPE, Pattern.compile("^[A-Z_][a-zA-Z0-9_]*")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^[.,;{}:^<>=\\\\/+*?#!-]")}));

        setShortcutStylePatterns(_shortcutStylePatterns);
        setFallthroughStylePatterns(_fallthroughStylePatterns);
    }

    public static List<String> getFileExtensions() {
        return Arrays.asList(new String[]{"logtalk", "lgt"});
    }
}