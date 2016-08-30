// Copyright (C) 2009 Google Inc.
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
 * This is similar to the lang-css.js in JavaScript Prettify.
 * 
 * All comments are adapted from the JavaScript Prettify.
 * 
 * <p>
 * Registers a language handler for CSS.
 *
 *
 * To use, include prettify.js and this file in your HTML page.
 * Then put your code in an HTML tag like
 *      <pre class="prettyprint lang-css"></pre>
 *
 *
 * http://www.w3.org/TR/CSS21/grammar.html Section G2 defines the lexical
 * grammar.  This scheme does not recognize keywords containing escapes.
 *
 * @author mikesamuel@gmail.com
 */
public class LangCss extends Lang {

  public LangCss() {
    List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
    List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

    // The space production <s>
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^[ \t\r\n\f]+"), null, " \t\r\n\f"}));
    // Quoted strings.  <string1> and <string2>
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^\\\"(?:[^\n\r\f\\\\\\\"]|\\\\(?:\r\n?|\n|\f)|\\\\[\\s\\S])*\\\""), null}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^\\'(?:[^\n\r\f\\\\\\']|\\\\(?:\r\n?|\n|\f)|\\\\[\\s\\S])*\\'"), null}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-css-str", Pattern.compile("^url\\(([^\\)\\\"\\']+)\\)", Pattern.CASE_INSENSITIVE)}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^(?:url|rgb|\\!important|@import|@page|@media|@charset|inherit)(?=[^\\-\\w]|$)", Pattern.CASE_INSENSITIVE), null}));
    // A property name -- an identifier followed by a colon.
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-css-kw", Pattern.compile("^(-?(?:[_a-z]|(?:\\\\[0-9a-f]+ ?))(?:[_a-z0-9\\-]|\\\\(?:\\\\[0-9a-f]+ ?))*)\\s*:", Pattern.CASE_INSENSITIVE)}));
    // A C style block comment.  The <comment> production.
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^\\/\\*[^*]*\\*+(?:[^\\/*][^*]*\\*+)*\\/")}));
    // Escaping text spans
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^(?:<!--|-->)")}));
    // A number possibly containing a suffix.
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^(?:\\d+|\\d*\\.\\d+)(?:%|[a-z]+)?", Pattern.CASE_INSENSITIVE)}));
    // A hex color
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^#(?:[0-9a-f]{3}){1,2}\\b", Pattern.CASE_INSENSITIVE)}));
    // An identifier
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^-?(?:[_a-z]|(?:\\\\[\\da-f]+ ?))(?:[_a-z\\d\\-]|\\\\(?:\\\\[\\da-f]+ ?))*", Pattern.CASE_INSENSITIVE)}));
    // A run of punctuation
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^[^\\s\\w\\'\\\"]+", Pattern.CASE_INSENSITIVE)}));

    setShortcutStylePatterns(_shortcutStylePatterns);
    setFallthroughStylePatterns(_fallthroughStylePatterns);

    setExtendedLangs(Arrays.asList(new Lang[]{new LangCssKeyword(), new LangCssString()}));
  }

  public static List<String> getFileExtensions() {
    return Arrays.asList(new String[]{"css"});
  }

  protected static class LangCssKeyword extends Lang {

    public LangCssKeyword() {
      List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
      List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

      _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^-?(?:[_a-z]|(?:\\\\[\\da-f]+ ?))(?:[_a-z\\d\\-]|\\\\(?:\\\\[\\da-f]+ ?))*", Pattern.CASE_INSENSITIVE)}));

      setShortcutStylePatterns(_shortcutStylePatterns);
      setFallthroughStylePatterns(_fallthroughStylePatterns);
    }

    public static List<String> getFileExtensions() {
      return Arrays.asList(new String[]{"css-kw"});
    }
  }

  protected static class LangCssString extends Lang {

    public LangCssString() {
      List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
      List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

      _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^[^\\)\\\"\\']+")}));

      setShortcutStylePatterns(_shortcutStylePatterns);
      setFallthroughStylePatterns(_fallthroughStylePatterns);
    }

    public static List<String> getFileExtensions() {
      return Arrays.asList(new String[]{"css-str"});
    }
  }
}
