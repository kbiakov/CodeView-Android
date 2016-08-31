// Copyright (C) 2013 Andrew Allen
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package io.github.kbiakov.codeview.highlight.prettify.lang;

import io.github.kbiakov.codeview.highlight.prettify.parser.Prettify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This is similar to the lang-erlang.js in JavaScript Prettify.
 * <p/>
 * All comments are adapted from the JavaScript Prettify.
 * <p/>
 * <p/>
 * <p/>
 * Derived from https://raw.github.com/erlang/otp/dev/lib/compiler/src/core_parse.yrl
 * Modified from Mike Samuel's Haskell plugin for google-code-prettify
 *
 * @author achew22@gmail.com
 */
public class LangErlang extends Lang {

    public LangErlang() {
        List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
        List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

        // Whitespace
        // whitechar    ->    newline | vertab | space | tab | uniWhite
        // newline      ->    return linefeed | return | linefeed | formfeed
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("\\t\\n\\x0B\\x0C\\r ]+"), null, "\t\n" + Character.toString((char) 0x0B) + Character.toString((char) 0x0C) + "\r "}));
        // Single line double-quoted strings.
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^\\\"(?:[^\\\"\\\\\\n\\x0C\\r]|\\\\[\\s\\S])*(?:\\\"|$)"), null, "\""}));

        // Handle atoms
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^[a-z][a-zA-Z0-9_]*")}));
        // Handle single quoted atoms
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^\\'(?:[^\\'\\\\\\n\\x0C\\r]|\\\\[^&])+\\'?"), null, "'"}));

        // Handle macros. Just to be extra clear on this one, it detects the ?
        // then uses the regexp to end it so be very careful about matching
        // all the terminal elements
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^\\?[^ \\t\\n({]+"), null, "?"}));

        // decimal      ->    digit{digit}
        // octal        ->    octit{octit}
        // hexadecimal  ->    hexit{hexit}
        // integer      ->    decimal
        //               |    0o octal | 0O octal
        //               |    0x hexadecimal | 0X hexadecimal
        // float        ->    decimal . decimal [exponent]
        //               |    decimal exponent
        // exponent     ->    (e | E) [+ | -] decimal
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^(?:0o[0-7]+|0x[\\da-f]+|\\d+(?:\\.\\d+)?(?:e[+\\-]?\\d+)?)", Pattern.CASE_INSENSITIVE), null, "0123456789"}));


        // TODO: catch @declarations inside comments

        // Comments in erlang are started with % and go till a newline
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^%[^\\n\\r]*")}));

         // Catch macros
         //[PR['PR_TAG'], /?[^( \n)]+/],

        /**
         * %% Keywords (atoms are assumed to always be single-quoted).
         * 'module' 'attributes' 'do' 'let' 'in' 'letrec'
         * 'apply' 'call' 'primop'
         * 'case' 'of' 'end' 'when' 'fun' 'try' 'catch' 'receive' 'after'
         */
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^(?:module|attributes|do|let|in|letrec|apply|call|primop|case|of|end|when|fun|try|catch|receive|after|char|integer|float,atom,string,var)\\b")}));

        /**
         * Catch definitions (usually defined at the top of the file)
         * Anything that starts -something
         */
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^-[a-z_]+")}));

        // Catch variables
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_TYPE, Pattern.compile("^[A-Z_][a-zA-Z0-9_]*")}));

        // matches the symbol production
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^[.,;]")}));

        setShortcutStylePatterns(_shortcutStylePatterns);
        setFallthroughStylePatterns(_fallthroughStylePatterns);
    }

    public static List<String> getFileExtensions() {
        return Arrays.asList(new String[]{"erlang", "erl"});
    }
}
