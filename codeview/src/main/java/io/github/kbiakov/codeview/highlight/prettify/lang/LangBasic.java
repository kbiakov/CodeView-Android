// Contributed by peter dot kofler at code minus cop dot org
package io.github.kbiakov.codeview.highlight.prettify.lang;

import io.github.kbiakov.codeview.highlight.prettify.parser.Prettify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This is similar to the lang-basic.js in JavaScript Prettify.
 * <p/>
 * To use, include prettify.js and this file in your HTML page.
 * Then put your code in an HTML tag like
 * <pre class="prettyprint lang-basic">(my BASIC code)</pre>
 *
 * @author peter dot kofler at code minus cop dot org
 */
public class LangBasic extends Lang {

    public LangBasic() {
        List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
        List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

        // "single-line-string"
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^(?:\"(?:[^\\\\\"\\r\\n]|\\\\.)*(?:\"|$))"), null, "\""}));
        // Whitespace
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^\\s+"), null, "\t\n\r " + Character.toString((char) 0xA0)}));

        // A line comment that starts with REM
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^REM[^\\r\\n]*"), null}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^\\b(?:AND|CLOSE|CLR|CMD|CONT|DATA|DEF ?FN|DIM|END|FOR|GET|GOSUB|GOTO|IF|INPUT|LET|LIST|LOAD|NEW|NEXT|NOT|ON|OPEN|OR|POKE|PRINT|READ|RESTORE|RETURN|RUN|SAVE|STEP|STOP|SYS|THEN|TO|VERIFY|WAIT)\\b"), null}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^[A-Z][A-Z0-9]?(?:\\$|%)?", Pattern.CASE_INSENSITIVE), null}));
        // Literals .0, 0, 0.0 0E13
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:e[+\\-]?\\d+)?", Pattern.CASE_INSENSITIVE), null, "0123456789"}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^.[^\\s\\w\\.$%\"]*"), null}));

        setShortcutStylePatterns(_shortcutStylePatterns);
        setFallthroughStylePatterns(_fallthroughStylePatterns);
    }

    public static List<String> getFileExtensions() {
        return Arrays.asList(new String[]{"basic", "cbm"});
    }
}
