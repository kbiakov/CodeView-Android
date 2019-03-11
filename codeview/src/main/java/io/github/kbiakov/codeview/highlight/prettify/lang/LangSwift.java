package io.github.kbiakov.codeview.highlight.prettify.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import io.github.kbiakov.codeview.highlight.prettify.parser.Prettify;

public class LangSwift extends Lang {

    public LangSwift() {
        List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
        List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^[ \\n\\r\\t\\v\\f\\\0]+"), null, " \\n\\r\\t\\v\\f\\0"}));
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^\"(?:[^\"\\\\]|(?:\\\\.)|(?:\\\\\\((?:[^\"\\\\)]|\\\\.)*\\)))*\""), null, "\""}));

        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^(?:(?:0x[\\da-fA-F][\\da-fA-F_]*\\.[\\da-fA-F][\\da-fA-F_]*[pP]?)|(?:\\d[\\d_]*\\.\\d[\\d_]*[eE]?))[+-]?\\d[\\d_]*"), null}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^-?(?:(?:0(?:(?:b[01][01_]*)|(?:o[0-7][0-7_]*)|(?:x[\\da-fA-F][\\da-fA-F_]*)))|(?:\\d[\\d_]*))"), null}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^(?:_|Any|true|false|nil)\\b"), null}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^\\b(?:__COLUMN__|__FILE__|__FUNCTION__|__LINE__|#available|#colorLiteral|#column|#else|#elseif|#endif|#file|#fileLiteral|#function|#if|#imageLiteral|#line|#selector|#sourceLocation|arch|arm|arm64|associatedtype|associativity|as|break|case|catch|class|continue|convenience|default|defer|deinit|didSet|do|dynamic|dynamicType|else|enum|extension|fallthrough|fileprivate|final|for|func|get|guard|import|indirect|infix|init|inout|internal|i386|if|in|iOS|iOSApplicationExtension|is|lazy|left|let|mutating|none|nonmutating|open|operator|optional|OSX|OSXApplicationExtension|override|postfix|precedence|prefix|private|protocol|Protocol|public|repeat|required|rethrows|return|right|safe|Self|self|set|static|struct|subscript|super|switch|throw|throws|try|Type|typealias|unowned|unsafe|var|weak|watchOS|where|while|willSet|x86_64)\\b"), null}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^\\/\\/.*?[\\n\\r]"), null}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^\\/\\*[\\s\\S]*?(?:\\*\\/|$)"), null}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^<<=|<=|<<|>>=|>=|>>|===|==|\\.\\.\\.|&&=|\\.\\.<|!==|!=|&=|~=|~|\\(|\\)|\\[|\\]|\\{|}|@|#|;|\\.|,|:|\\|\\|=|\\?\\?|\\|\\||&&|&\\*|&\\+|&-|&=|\\+=|-=|\\/=|\\*=|\\^=|%=|\\|=|->|`|==|\\+\\+|--|\\/|\\+|!|\\*|%|<|>|&|\\||\\^|\\?|=|-|_"), null}));
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_TYPE, Pattern.compile("^\\b(?:[@_]?[A-Z]+[a-z][A-Za-z_$@0-9]*|\\w+_t\\b)"), null}));

        setShortcutStylePatterns(_shortcutStylePatterns);
        setFallthroughStylePatterns(_fallthroughStylePatterns);
    }

    public static List<String> getFileExtensions() {
        return Arrays.asList(new String[]{"swift"});
    }
}