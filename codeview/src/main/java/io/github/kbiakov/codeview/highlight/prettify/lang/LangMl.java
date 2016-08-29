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
 * This is similar to the lang-ml.js in JavaScript Prettify.
 * 
 * All comments are adapted from the JavaScript Prettify.
 * 
 * <p>
 * Registers a language handler for OCaml, SML, F# and similar languages.
 *
 * Based on the lexical grammar at
 * http://research.microsoft.com/en-us/um/cambridge/projects/fsharp/manual/spec.html#_Toc270597388
 *
 * @author mikesamuel@gmail.com
 */
public class LangMl extends Lang {

  public LangMl() {
    List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
    List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

    // Whitespace is made up of spaces, tabs and newline characters.
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^[\\t\\n\\r \\xA0]+"), null, "\t\n\r " + Character.toString((char) 0xA0)}));
    // #if ident/#else/#endif directives delimit conditional compilation
    // sections
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^#(?:if[\\t\\n\\r \\xA0]+(?:[a-z_$][\\w\\']*|``[^\\r\\n\\t`]*(?:``|$))|else|endif|light)", Pattern.CASE_INSENSITIVE), null, "#"}));
    // A double or single quoted, possibly multi-line, string.
    // F# allows escaped newlines in strings.
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^(?:\\\"(?:[^\\\"\\\\]|\\\\[\\s\\S])*(?:\\\"|$)|\\'(?:[^\\'\\\\]|\\\\[\\s\\S])(?:\\'|$))"), null, "\"'"}));
    // Block comments are delimited by (* and *) and may be
    // nested. Single-line comments begin with // and extend to
    // the end of a line.
    // TODO: (*...*) comments can be nested.  This does not handle that.
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^(?:\\/\\/[^\\r\\n]*|\\(\\*[\\s\\S]*?\\*\\))")}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^(?:abstract|and|as|assert|begin|class|default|delegate|do|done|downcast|downto|elif|else|end|exception|extern|false|finally|for|fun|function|if|in|inherit|inline|interface|internal|lazy|let|match|member|module|mutable|namespace|new|null|of|open|or|override|private|public|rec|return|static|struct|then|to|true|try|type|upcast|use|val|void|when|while|with|yield|asr|land|lor|lsl|lsr|lxor|mod|sig|atomic|break|checked|component|const|constraint|constructor|continue|eager|event|external|fixed|functor|global|include|method|mixin|object|parallel|process|protected|pure|sealed|trait|virtual|volatile)\\b")}));
    // A number is a hex integer literal, a decimal real literal, or in
    // scientific notation.
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^[+\\-]?(?:0x[\\da-f]+|(?:(?:\\.\\d+|\\d+(?:\\.\\d*)?)(?:e[+\\-]?\\d+)?))", Pattern.CASE_INSENSITIVE)}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^(?:[a-z_][\\w']*[!?#]?|``[^\\r\\n\\t`]*(?:``|$))", Pattern.CASE_INSENSITIVE)}));
    // A printable non-space non-special character
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^[^\\t\\n\\r \\xA0\\\"\\'\\w]+")}));

    setShortcutStylePatterns(_shortcutStylePatterns);
    setFallthroughStylePatterns(_fallthroughStylePatterns);
  }

  public static List<String> getFileExtensions() {
    return Arrays.asList(new String[]{"fs", "ml"});
  }
}
