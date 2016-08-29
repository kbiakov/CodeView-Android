// Copyright (C) 2008 Google Inc.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import io.github.kbiakov.codeview.highlight.prettify.parser.Prettify;

/**
 * This is similar to the lang-lua.js in JavaScript Prettify.
 * 
 * All comments are adapted from the JavaScript Prettify.
 * 
 * <p>
 * Registers a language handler for Lua.
 *
 *
 * To use, include prettify.js and this file in your HTML page.
 * Then put your code in an HTML tag like
 *      <pre class="prettyprint lang-lua">(my Lua code)</pre>
 *
 *
 * I used http://www.lua.org/manual/5.1/manual.html#2.1
 * Because of the long-bracket concept used in strings and comments, Lua does
 * not have a regular lexical grammar, but luckily it fits within the space
 * of irregular grammars supported by javascript regular expressions.
 *
 * @author mikesamuel@gmail.com
 */
public class LangLua extends Lang {

  public LangLua() {
    List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
    List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

    // Whitespace
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^[\t\n\r \\xA0]+"), null, "\t\n\r " + Character.toString((char) 0xA0)}));
    // A double or single quoted, possibly multi-line, string.
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^(?:\\\"(?:[^\\\"\\\\]|\\\\[\\s\\S])*(?:\\\"|$)|\\'(?:[^\\'\\\\]|\\\\[\\s\\S])*(?:\\'|$))"), null, "\"'"}));
    // A comment is either a line comment that starts with two dashes, or
    // two dashes preceding a long bracketed block.
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^--(?:\\[(=*)\\[[\\s\\S]*?(?:\\]\\1\\]|$)|[^\\r\\n]*)")}));
    // A long bracketed block not preceded by -- is a string.
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^\\[(=*)\\[[\\s\\S]*?(?:\\]\\1\\]|$)")}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^(?:and|break|do|else|elseif|end|false|for|function|if|in|local|nil|not|or|repeat|return|then|true|until|while)\\b"), null}));
    // A number is a hex integer literal, a decimal real literal, or in
    // scientific notation.
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^[+-]?(?:0x[\\da-f]+|(?:(?:\\.\\d+|\\d+(?:\\.\\d*)?)(?:e[+\\-]?\\d+)?))", Pattern.CASE_INSENSITIVE)}));
    // An identifier
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^[a-z_]\\w*", Pattern.CASE_INSENSITIVE)}));
    // A run of punctuation
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^[^\\w\\t\\n\\r \\xA0][^\\w\\n\\r \\xA0\\\"\\'\\-\\+=]*")}));

    setShortcutStylePatterns(_shortcutStylePatterns);
    setFallthroughStylePatterns(_fallthroughStylePatterns);
  }

  public static List<String> getFileExtensions() {
    return Arrays.asList(new String[]{"lua"});
  }
}
