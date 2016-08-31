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
 * This is similar to the lang-wiki.js in JavaScript Prettify.
 * 
 * All comments are adapted from the JavaScript Prettify.
 * 
 * <p>
 * Registers a language handler for Wiki pages.
 *
 * Based on WikiSyntax at http://code.google.com/p/support/wiki/WikiSyntax
 *
 * @author mikesamuel@gmail.com
 */
public class LangWiki extends Lang {

  public LangWiki() {
    List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
    List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

    // Whitespace
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^[\\t \\xA0a-gi-z0-9]+"), null, "\t " + Character.toString((char) 0xA0) + "abcdefgijklmnopqrstuvwxyz0123456789"}));
    // Wiki formatting
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^[=*~\\^\\[\\]]+"), null, "=*~^[]"}));
    // Meta-info like #summary, #labels, etc.
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-wiki.meta", Pattern.compile("(?:^^|\r\n?|\n)(#[a-z]+)\\b")}));
    // A WikiWord
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^(?:[A-Z][a-z][a-z0-9]+[A-Z][a-z][a-zA-Z0-9]+)\\b")}));
    // A preformatted block in an unknown language
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-", Pattern.compile("^\\{\\{\\{([\\s\\S]+?)\\}\\}\\}")}));
    // A block of source code in an unknown language
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-", Pattern.compile("^`([^\r\n`]+)`")}));
    // An inline URL.
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^https?:\\/\\/[^\\/?#\\s]*(?:\\/[^?#\\s]*)?(?:\\?[^#\\s]*)?(?:#\\S*)?", Pattern.CASE_INSENSITIVE)}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^(?:\r\n|[\\s\\S])[^#=*~^A-Zh\\{`\\[\r\n]*")}));

    setShortcutStylePatterns(_shortcutStylePatterns);
    setFallthroughStylePatterns(_fallthroughStylePatterns);

    setExtendedLangs(Arrays.asList(new Lang[]{new LangWikiMeta()}));
  }

  public static List<String> getFileExtensions() {
    return Arrays.asList(new String[]{"wiki"});
  }

  protected static class LangWikiMeta extends Lang {

    public LangWikiMeta() {
      List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
      List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

      _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^#[a-z]+", Pattern.CASE_INSENSITIVE), null, "#"}));

      setShortcutStylePatterns(_shortcutStylePatterns);
      setFallthroughStylePatterns(_fallthroughStylePatterns);
    }

    public static List<String> getFileExtensions() {
      return Arrays.asList(new String[]{"wiki.meta"});
    }
  }
}
