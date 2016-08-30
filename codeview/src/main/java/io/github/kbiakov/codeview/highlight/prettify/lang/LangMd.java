package io.github.kbiakov.codeview.highlight.prettify.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import io.github.kbiakov.codeview.highlight.prettify.parser.Prettify;

/**
 * Registers a language handler for markdown.
 *
 * @author Kirill Biakov (kbiakov@gmail.com)
 */
public class LangMd extends Lang {

    public LangMd() {
        List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
        List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_DECLARATION, Pattern.compile("^#.*?[\\n\\r]")}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^```[\\s\\S]*?(?:```|$)")}));

        setShortcutStylePatterns(_shortcutStylePatterns);
        setFallthroughStylePatterns(_fallthroughStylePatterns);
    }

    public static List<String> getFileExtensions() {
        return Arrays.asList(new String[]{"md", "markdown"});
    }
}
