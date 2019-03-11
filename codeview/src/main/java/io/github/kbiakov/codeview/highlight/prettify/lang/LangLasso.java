package io.github.kbiakov.codeview.highlight.prettify.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import io.github.kbiakov.codeview.highlight.prettify.parser.Prettify;

public class LangLasso extends Lang {

    public LangLasso() {
        List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
        List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^[\\t\\n\\r \\xA0]+"), null, "\\t\\n\\r \\xA0"}));
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^\\'[^\\'\\\\]*(?:\\\\[\\s\\S][^\\'\\\\]*)*(?:\\'|$)"), null, "\'"}));
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^\\\"[^\\\"\\\\]*(?:\\\\[\\s\\S][^\\\"\\\\]*)*(?:\\\"|$)"), null, "\""}));
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^\\`[^\\`]*(?:\\`|$)"), null, "`"}));
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^0x[\\da-f]+|\\d+", Pattern.CASE_INSENSITIVE), null, "0123456789"}));
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_ATTRIB_NAME, Pattern.compile("^[#$][a-z_][\\w.]*|#\\d+\\b|#![ \\S]+lasso9\\b", Pattern.CASE_INSENSITIVE), null, "#$"}));

        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_TAG, Pattern.compile("^[\\[\\]]|<\\?(?:lasso(?:script)?|=)|\\?>|(no_square_brackets|noprocess)\\b", Pattern.CASE_INSENSITIVE)}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^\\/\\/[^\\r\\n]*|\\/\\*[\\s\\S]*?\\*\\/")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_ATTRIB_NAME, Pattern.compile("^-(?!infinity)[a-z_][\\w.]*|\\.\\s*'[a-z_][\\w.]*'|\\.{3}", Pattern.CASE_INSENSITIVE)}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^\\d*\\.\\d+(?:e[-+]?\\d+)?|(infinity|NaN)\\b", Pattern.CASE_INSENSITIVE)}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_ATTRIB_VALUE, Pattern.compile("^::\\s*[a-z_][\\w.]*", Pattern.CASE_INSENSITIVE)}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^(?:true|false|none|minimal|full|all|void|and|or|not|bw|nbw|ew|new|cn|ncn|lt|lte|gt|gte|eq|neq|rx|nrx|ft)\\b", Pattern.CASE_INSENSITIVE)}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_TYPE, Pattern.compile("^(?:array|date|decimal|duration|integer|map|pair|string|tag|xml|null|boolean|bytes|keyword|list|locale|queue|set|stack|staticarray|local|var|variable|global|data|self|inherited|currentcapture|givenblock)\\b|^\\.\\.?", Pattern.CASE_INSENSITIVE)}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^(?:cache|database_names|database_schemanames|database_tablenames|define_tag|define_type|email_batch|encode_set|html_comment|handle|handle_error|header|if|inline|iterate|ljax_target|link|link_currentaction|link_currentgroup|link_currentrecord|link_detail|link_firstgroup|link_firstrecord|link_lastgroup|link_lastrecord|link_nextgroup|link_nextrecord|link_prevgroup|link_prevrecord|log|loop|namespace_using|output_none|portal|private|protect|records|referer|referrer|repeating|resultset|rows|search_args|search_arguments|select|sort_args|sort_arguments|thread_atomic|value_list|while|abort|case|else|fail_if|fail_ifnot|fail|if_empty|if_false|if_null|if_true|loop_abort|loop_continue|loop_count|params|params_up|return|return_value|run_children|soap_definetag|soap_lastrequest|soap_lastresponse|tag_name|ascending|average|by|define|descending|do|equals|frozen|group|handle_failure|import|in|into|join|let|match|max|min|on|order|parent|protected|provide|public|require|returnhome|skip|split_thread|sum|take|thread|to|trait|type|where|with|yield|yieldhome)\\b", Pattern.CASE_INSENSITIVE)}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^[a-z_][\\w.]*(?:=\\s*(?=\\())?", Pattern.CASE_INSENSITIVE)}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^:=|[-+*\\/%=<>&|!?\\\\]+")}));

        setShortcutStylePatterns(_shortcutStylePatterns);
        setFallthroughStylePatterns(_fallthroughStylePatterns);
    }

    public static List<String> getFileExtensions() {
        return Arrays.asList(new String[]{"lasso", "ls", "lassoscript"});
    }
}
